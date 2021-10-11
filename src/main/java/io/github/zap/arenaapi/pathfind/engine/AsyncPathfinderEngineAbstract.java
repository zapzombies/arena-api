package io.github.zap.arenaapi.pathfind.engine;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.context.AsyncPathfinderContext;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.commons.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

abstract class AsyncPathfinderEngineAbstract<T extends AsyncPathfinderContext> implements PathfinderEngine, Listener {
    protected static final int MAX_THREADS = 2;
    protected static final int UNLOAD_TIME_INTERVAL = 10;
    protected static final TimeUnit UNLOAD_TIME_UNIT = TimeUnit.SECONDS;

    private final Map<UUID, T> contexts;
    private final Event<WorldUnloadEvent> worldUnloadEvent;
    protected final Plugin plugin;
    protected final WorldBridge bridge;

    AsyncPathfinderEngineAbstract(@NotNull Map<UUID, T> contexts, @NotNull Plugin plugin, @NotNull WorldBridge bridge) {
        this.contexts = contexts;
        worldUnloadEvent = Event.bukkitProxy(plugin, WorldUnloadEvent.class, EventPriority.MONITOR, true);
        worldUnloadEvent.addHandler(this::onWorldUnload);
        this.plugin = plugin;
        this.bridge = bridge;
    }

    @Override
    public @NotNull Future<PathResult> giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        T context = contexts.computeIfAbsent(world.getUID(), (key) -> makeContext(makeBlockCollisionProvider(world)));

        return context.executor().submit(() -> {
            try {
                return processOperation(context, operation);
            }
            catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Exception thrown in PathOperation handler", exception);
            }

            return null;
        });

    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void unload() {
        worldUnloadEvent.clearHandlers();

        for(AsyncPathfinderContext context : contexts.values()) {
            unloadAndAwaitContext(context);
        }

        contexts.clear();
    }

    protected @Nullable PathResult processOperation(@NotNull T context, @NotNull PathOperation operation) {
        while(operation.state() == PathOperation.State.STARTED) {
            for(int i = 0; i < operation.iterations(); i++) {
                if(operation.step(context)) {
                    PathResult result = operation.result();
                    context.recordPath(result);
                    return result;
                }
            }

            if(Thread.interrupted()) {
                plugin.getLogger().log(Level.WARNING, "processOperation interrupted for PathOperation. " +
                        "Returning null PathResult");
                return null;
            }
        }

        return operation.result();
    }

    protected abstract @NotNull T makeContext(@NotNull BlockCollisionProvider provider);

    protected abstract @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world);

    private void onWorldUnload(Object sender, WorldUnloadEvent event) {
        UUID worldID = event.getWorld().getUID();
        AsyncPathfinderContext context = contexts.remove(worldID);

        if(context != null) {
            unloadAndAwaitContext(context);
        }
    }

    private void unloadAndAwaitContext(AsyncPathfinderContext context) {
        ExecutorService executorService = context.executor();
        executorService.shutdown();

        try {
            if(!executorService.awaitTermination(UNLOAD_TIME_INTERVAL, UNLOAD_TIME_UNIT)) {
                plugin.getLogger().warning("Pathfinder service failed to terminate after " + UNLOAD_TIME_INTERVAL
                        + " " + UNLOAD_TIME_UNIT + " in context " + context);
                executorService.shutdownNow();
            }
            else {
                plugin.getLogger().info("Pathfinding context " + context + " unloaded");
            }
        }
        catch (InterruptedException exception) {
            plugin.getLogger().log(Level.WARNING, "Interrupted when waiting for pathfinder service " +
                    "termination in context " + context, exception);
        }
    }
}

package io.github.zap.arenaapi.pathfind.engine;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProviders;
import io.github.zap.arenaapi.pathfind.context.AsyncPathfinderContext;
import io.github.zap.arenaapi.pathfind.process.PathMergers;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.Executors;

class AsyncProxyPathfinderEngine extends AsyncPathfinderEngineAbstract<AsyncPathfinderContext> {
    private static final int PATH_CAPACITY = 32;

    AsyncProxyPathfinderEngine(@NotNull Plugin plugin, @NotNull WorldBridge bridge) {
        super(new HashMap<>(), plugin, bridge);
    }

    @NotNull
    @Override
    protected AsyncPathfinderContext makeContext(@NotNull BlockCollisionProvider provider) {
        plugin.getLogger().info("Creating pathfinder context for world " + provider.world().getName());
        return new AsyncPathfinderContext(Executors.newWorkStealingPool(MAX_THREADS), provider,
                PathMergers.defaultMerger(), PATH_CAPACITY);
    }

    @Override
    protected @NotNull BlockCollisionProvider makeBlockCollisionProvider(@NotNull World world) {
        return BlockCollisionProviders.proxyAsyncProvider(plugin, bridge, world);
    }
}

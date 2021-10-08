package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.commons.event.Event;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ProxyBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final WorldBridge worldBridge;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Event<ChunkUnloadEvent> chunkUnloadEvent;

    ProxyBlockCollisionProvider(@NotNull Plugin plugin, @NotNull WorldBridge worldBridge, @NotNull World world) {
        super(world, new Long2ObjectOpenHashMap<>(), true);
        this.worldBridge = worldBridge;
        chunkUnloadEvent = Event.bukkitProxy(plugin, ChunkUnloadEvent.class, EventPriority.MONITOR, true);
        chunkUnloadEvent.addHandler(this::onChunkUnload);
    }

    private void onChunkUnload(Object sender, ChunkUnloadEvent args) {
        Chunk chunk = args.getChunk();
        long chunkKey = chunkKey(chunk.getX(), chunk.getZ());

        rwl.writeLock().lock();
        chunkViewMap.remove(chunkKey);
        rwl.writeLock().unlock();
    }

    @Override
    public void unload() {
        chunkUnloadEvent.clearHandlers();

        rwl.writeLock().lock();
        chunkViewMap.clear();
        rwl.writeLock().unlock();
    }

    @Override
    public boolean hasChunk(int x, int z) {
        //this is definitely safe to access async!
        return world.isChunkLoaded(x, z);
    }

    @Override
    public @Nullable CollisionChunkView chunkAt(int x, int z) {
        long key = chunkKey(x, z);

        rwl.readLock().lock();
        CollisionChunkView view = chunkViewMap.get(key);
        rwl.readLock().unlock();

        if(view == null) {
            Chunk chunk = worldBridge.getChunkIfLoadedImmediately(world, x, z);

            if(chunk != null) {
                view = worldBridge.proxyView(chunk);

                rwl.writeLock().lock();
                chunkViewMap.put(key, view);
                rwl.writeLock().unlock();
            }
        }

        return view;
    }
}

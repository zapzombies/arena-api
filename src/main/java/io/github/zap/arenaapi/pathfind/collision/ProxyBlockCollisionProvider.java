package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantReadWriteLock;

class ProxyBlockCollisionProvider extends BlockCollisionProviderAbstract {
    private final WorldBridge worldBridge;
    private final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();

    ProxyBlockCollisionProvider(@NotNull WorldBridge worldBridge, @NotNull World world, int concurrency) {
        super(world, new Long2ObjectOpenHashMap<>(), true);
        this.worldBridge = worldBridge;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        //this is definitely safe to access async!
        return world.isChunkLoaded(x, z);
    }

    @Override
    public @Nullable CollisionChunkView chunkAt(int x, int z) {
        long key = chunkKey(x, z);

        rrwl.readLock().lock();
        CollisionChunkView view = chunkViewMap.get(key);
        rrwl.readLock().unlock();

        if(view == null) {
            Chunk chunk = worldBridge.getChunkIfLoadedImmediately(world, x, z);

            if(chunk != null) {
                view = worldBridge.proxyView(chunk);

                rrwl.writeLock().lock();
                chunkViewMap.put(key, view);
                rrwl.writeLock().unlock();
            }
        }

        return view;
    }
}

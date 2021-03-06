package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.util.Direction;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vector3I;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This interface provides a general template for a class which provides block state information over a limited
 * region in a single dimension. If implementations are to be used with asynchronous PathfinderEngines, certain methods
 * must, at the very least, allow safe access from threads other than the main server thread. It is NOT necessary for
 * implementations to support concurrency in any capacity, and may throw exceptions when multiple threads attempt to
 * call a method concurrently.
 */
public interface BlockCollisionProvider {
    /**
     * Returns the World object this BlockProvider is linked to.
     * @return The Bukkit World this BlockProvider uses. Operations on this object are generally not thread-safe.
     */
    @NotNull World world();

    /**
     * Returns whether or not this BlockCollisionProvider supports asynchronous access; that is, access by threads other
     * than the main server. This does not guarantee any sort of safety in regards to concurrent access.
     * @return True if this objects supports access off of the main server thread, false otherwise
     */
    boolean supportsAsync();

    void updateRegion(@NotNull ChunkBounds coordinates);

    void clearRegion(@NotNull ChunkBounds coordinates);

    void clearForWorld();

    boolean hasChunk(int x, int z);

    @Nullable CollisionChunkView chunkAt(int x, int z);

    @Nullable BlockCollisionView getBlock(int x, int y, int z);

    boolean collidesAt(@NotNull BoundingBox bounds);

    @NotNull List<BlockCollisionView> collidingSolidsAt(@NotNull BoundingBox bounds);

    boolean collidesMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Direction direction, @NotNull Vector3D translation);

    default @Nullable BlockCollisionView getBlock(@NotNull Vector3I at) {
        return getBlock(at.x(), at.y(), at.z());
    }
}

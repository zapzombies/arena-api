package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.util.BoundedBlockIterator;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.BlockSource;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.commons.vectors.Vector2I;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class CollisionChunkAbstract_v1_16_R3 implements CollisionChunkView {
    protected final int x;
    protected final int z;

    protected final int originX;
    protected final int originZ;

    private final BoundingBox chunkBounds;
    private final BlockSource source = (x, y, z) ->
            CollisionChunkAbstract_v1_16_R3.this.getBlock(x & 15, y, z & 15);

    CollisionChunkAbstract_v1_16_R3(int locX, int locZ) {
        this.x = locX;
        this.z = locZ;

        this.originX = x << 4;
        this.originZ = z << 4;

        this.chunkBounds = new BoundingBox(originX, 0, originZ, originX + 16, 255, originZ + 16);
    }

    @Override
    public @NotNull Vector2I position() {
        return Vectors.of(x, z);
    }

    @Override
    public boolean collidesWithAny(@NotNull BoundingBox worldBounds) {
        if(chunkBounds.overlaps(worldBounds)) {
            BoundingBox overlap = worldBounds.clone().intersection(chunkBounds);
            Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(source, overlap);

            while(iterator.hasNext()) {
                BlockCollisionView snapshot = iterator.next();

                if(snapshot != null && snapshot.overlaps(overlap)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockCollisionView> collisionsWith(@NotNull BoundingBox worldBounds) {
        List<BlockCollisionView> shapes = new ArrayList<>();

        if(worldBounds.overlaps(chunkBounds)) {
            BoundingBox overlap = worldBounds.clone().intersection(chunkBounds);
            Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(source, overlap);

            while(iterator.hasNext()) {
                BlockCollisionView view = iterator.next();

                if(view != null && view.overlaps(overlap)) {
                    shapes.add(view);
                }
            }
        }

        return shapes;
    }

    protected void assertValidChunkCoordinate(int x, int y, int z) {
        if(x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
            throw new IllegalArgumentException("Invalid chunk coordinates [" + x + ", " + y + ", " + z + "]");
        }
    }
}
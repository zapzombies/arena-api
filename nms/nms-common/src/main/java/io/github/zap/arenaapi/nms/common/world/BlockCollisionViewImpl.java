package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.commons.vectors.Direction;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

record BlockCollisionViewImpl(int x, int y, int z, BlockData data, VoxelShapeWrapper collision) implements BlockCollisionView {
    @Override
    public boolean overlaps(@NotNull BoundingBox worldBounds) {
        return collision.overlaps(
                (worldBounds.getMinX() - x) + Vectors.EPSILON,
                (worldBounds.getMinY() - y) + Vectors.EPSILON,
                (worldBounds.getMinZ() - z) + Vectors.EPSILON,
                (worldBounds.getMaxX() - x) - Vectors.EPSILON,
                (worldBounds.getMaxY() - y) - Vectors.EPSILON,
                (worldBounds.getMaxZ() - z) - Vectors.EPSILON);
    }

    @Override
    public double exactY() {
        if(!collision.isEmpty()) {
            return y + collision.boundingBox().positionDirectional(Direction.UP).minY();
        }

        return y;
    }
}

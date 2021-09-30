package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.commons.vectors.Vector3I;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface BlockCollisionView extends Vector3I {
    @NotNull BlockData data();

    @NotNull VoxelShapeWrapper collision();

    boolean isOverlapping(@NotNull BoundingBox worldBounds);

    double exactY();

    static BlockCollisionView from(int x, int y, int z, @NotNull BlockData data,
                                   @NotNull VoxelShapeWrapper shape) {
        return new BlockCollisionViewImpl(x, y, z, data, shape);
    }
}

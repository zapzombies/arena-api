package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.commons.vectors.Bounds;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface VoxelShapeWrapper extends Iterable<Bounds> {
    boolean isFull();

    boolean isEmpty();

    boolean isPartial();

    int size();

    @NotNull Bounds shapeAt(int index);

    Bounds boundingBox();

    boolean anyBoundsMatches(@NotNull BoxPredicate predicate);

    boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    default boolean collidesWith(@NotNull BoundingBox boundingBox) {
        return collidesWith(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
}


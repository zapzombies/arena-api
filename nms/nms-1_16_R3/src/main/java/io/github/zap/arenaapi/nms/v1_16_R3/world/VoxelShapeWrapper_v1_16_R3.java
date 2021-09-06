package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.commons.vectors.Bounds;
import net.minecraft.server.v1_16_R3.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper FULL = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.fullCube());
    public static final VoxelShapeWrapper EMPTY = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.empty());

    private final VoxelShape shape;
    private final Bounds boundingBox;
    private Bounds[] shapes = null;

    VoxelShapeWrapper_v1_16_R3(VoxelShape shape) {
        this.shape = shape;
        AxisAlignedBB bb = shape.getBoundingBox();
        boundingBox = new Bounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    private Bounds[] getShapes() {
        if(shapes == null) {
            List<AxisAlignedBB> aabbs = shape.d();
            shapes = new Bounds[aabbs.size()];

            int i = 0;
            for(AxisAlignedBB bb : aabbs) {
                shapes[i++] = new Bounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY,bb.maxZ);
            }
        }

        return shapes;
    }

    @Override
    public boolean isFull() {
        return shape == VoxelShapes.fullCube();
    }

    @Override
    public boolean isEmpty() {
        return shape == VoxelShapes.empty();
    }

    @Override
    public boolean isPartial() {
        return shape != VoxelShapes.empty() && shape != VoxelShapes.fullCube();
    }

    @Override
    public int size() {
        return getShapes().length;
    }

    @Override
    public @NotNull Bounds shapeAt(int index) {
        return getShapes()[index];
    }

    @Override
    public @NotNull Bounds boundingBox() {
        return boundingBox;
    }

    @Override
    public boolean anyBoundsMatches(@NotNull BoxPredicate predicate) {
        for(Bounds bounds : getShapes()) {
            if(predicate.test(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean collidesWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for(Bounds bound : getShapes()) {
            if(bound.overlaps(minX, minY, minZ, maxX, maxY, maxZ)) {
                return true;
            }
        }

        return false;
    }
}
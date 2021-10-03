package io.github.zap.arenaapi.nms.v1_16_R3.world;

import com.google.common.collect.Iterators;
import io.github.zap.arenaapi.nms.common.world.BoxPredicate;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.commons.vectors.Bounds;
import net.minecraft.server.v1_16_R3.*;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

class VoxelShapeWrapper_v1_16_R3 implements VoxelShapeWrapper {
    public static final VoxelShapeWrapper FULL = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.fullCube());
    public static final VoxelShapeWrapper EMPTY = new VoxelShapeWrapper_v1_16_R3(VoxelShapes.empty());

    private final VoxelShape shape;
    private final Bounds boundingBox;
    private final Bounds[] shapes;

    VoxelShapeWrapper_v1_16_R3(@NotNull VoxelShape shape) {
        this.shape = shape;

        if(!shape.isEmpty()) {
            AxisAlignedBB bb = shape.getBoundingBox();
            boundingBox = new Bounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);

            List<AxisAlignedBB> aabbs = shape.d();
            shapes = new Bounds[aabbs.size()];

            int i = 0;
            for(AxisAlignedBB bounds : aabbs) {
                shapes[i++] = new Bounds(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ);
            }
        }
        else {
            boundingBox = null;
            shapes = new Bounds[0];
        }
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
        return shapes.length;
    }

    @Override
    public @NotNull Bounds shapeAt(int index) {
        return shapes[index];
    }

    @Override
    public Bounds boundingBox() {
        return boundingBox;
    }

    @Override
    public boolean anyBoundsMatches(@NotNull BoxPredicate predicate) {
        for(Bounds bounds : shapes) {
            if(predicate.test(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for(Bounds bound : shapes) {
            if(bound.overlaps(minX, minY, minZ, maxX, maxY, maxZ)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<Bounds> iterator() {
        return Iterators.forArray(shapes);
    }
}
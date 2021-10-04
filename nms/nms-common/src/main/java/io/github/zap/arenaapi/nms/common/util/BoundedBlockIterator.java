package io.github.zap.arenaapi.nms.common.util;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.BlockSource;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BoundedBlockIterator implements Iterator<BlockCollisionView> {
    private final BlockSource source;
    private final int startX;
    private final int startY;

    private final int endX;
    private final int endY;
    private final int endZ;

    private int x;
    private int y;
    private int z;

    public BoundedBlockIterator(@NotNull BlockSource source, @NotNull BoundingBox bounds) {
        this.source = source;

        startX = NumberConversions.floor(bounds.getMinX() + Vectors.EPSILON);
        startY = NumberConversions.floor(bounds.getMinY() + Vectors.EPSILON);

        x = startX - 1;
        y = startY;
        z = NumberConversions.floor(bounds.getMinZ() + Vectors.EPSILON);

        endX = NumberConversions.floor(bounds.getMaxX() - Vectors.EPSILON) + 1;
        endY = NumberConversions.floor(bounds.getMaxY() - Vectors.EPSILON) + 1;
        endZ = NumberConversions.floor(bounds.getMaxZ() - Vectors.EPSILON) + 1;
    }

    @Override
    public boolean hasNext() {
        int nextX = x + 1;
        int nextY = y;
        int nextZ = z;

        if(nextX >= endX) {
            nextY++;

            if(nextY >= endY) {
                nextZ++;

                return nextZ < endZ;
            }
        }

        return true;
    }

    @Override
    public BlockCollisionView next() {
        if(++x >= endX) {
            if(++y >= endY) {
                if(++z >= endZ) {
                    throw new NoSuchElementException();
                }

                y = startY;
            }

            x = startX;
        }

        return source.getBlock(x, y, z);
    }
}

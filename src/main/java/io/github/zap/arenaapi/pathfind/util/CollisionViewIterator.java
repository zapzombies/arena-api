package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

public class CollisionViewIterator implements Iterator<BlockCollisionView> {
    private final BlockCollisionProvider provider;
    private final Predicate<BlockCollisionView> viewPredicate;

    private final int minX;
    private final int minY;

    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private int x;
    private int y;
    private int z;

    private BlockCollisionView nextCache = null;

    public CollisionViewIterator(@NotNull BlockCollisionProvider provider, @NotNull BoundingBox boundingBox,
                                 @NotNull Predicate<BlockCollisionView> viewPredicate) {
        this.provider = Objects.requireNonNull(provider, "provider cannot be null");
        this.viewPredicate = Objects.requireNonNull(viewPredicate, "viewPredicate cannot be null");

        minX = NumberConversions.floor(boundingBox.getMinX());
        minY = NumberConversions.floor(boundingBox.getMinY());

        maxX = NumberConversions.floor(boundingBox.getMaxX());
        maxY = NumberConversions.floor(boundingBox.getMaxY());
        maxZ = NumberConversions.floor(boundingBox.getMaxZ());

        x = minX;
        y = minY;
        z = NumberConversions.floor(boundingBox.getMinZ());
    }

    private boolean hasNextInternal() {
        for(; x <= maxX; x++) {
            for(; y <= maxY; y++) {
                for(; z <= maxZ; z++) {
                    BlockCollisionView view = provider.getBlock(x, y, z);

                    if(view != null && viewPredicate.test(view)) {
                        nextCache = view;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasNext() {
        return nextCache != null || hasNextInternal();
    }

    @Override
    public BlockCollisionView next() {
        if(nextCache == null) {
            if(!hasNextInternal()) {
                throw new NoSuchElementException();
            }
        }

        x++;
        if(x > maxX) {
            x = minX;
            y++;
            if(y > maxY) {
                y = minY;
                z++;
            }
        }

        BlockCollisionView cacheSave = nextCache;
        nextCache = null;

        return cacheSave;
    }
}

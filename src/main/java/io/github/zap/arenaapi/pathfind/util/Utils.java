package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Utils {
    public static boolean isValidLocation(@NotNull Location location) {
        return location.getWorld().getWorldBorder().isInside(location) && location.getY() >= 0 && location.getY() < 256;
    }

    public static BlockCollisionView highestBlockBelow(@NotNull BlockCollisionProvider provider, @NotNull BoundingBox boundingBox) {
        BoundingBox shrunkenBounds = boundingBox.clone().resize(
                boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMinY() + 1, boundingBox.getMaxZ());

        List<BlockCollisionView> views;
        do {
            shrunkenBounds.shift(0, -1, 0);
            views = provider.solidsOverlapping(shrunkenBounds);
        }
        while(views.isEmpty() && shrunkenBounds.getMinY() >= 1);

        BlockCollisionView highestView = null;
        double highestY = Double.MIN_VALUE;
        for(BlockCollisionView view : views) {
            double thisY = view.exactY();

            if(thisY > highestY) {
                highestY = thisY;
                highestView = view;
            }
        }

        if(highestView != null) {
            return highestView;
        }

        return provider.getBlock(Vectors.asIntFloor(boundingBox.getCenterX(), 0, boundingBox.getCenterZ()));
    }
}

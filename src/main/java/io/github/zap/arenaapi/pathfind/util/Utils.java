package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.arenaapi.nms.common.util.BoundedBlockIterator;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isValidLocation(@NotNull Location location) {
        return location.getWorld().getWorldBorder().isInside(location) && location.getY() >= 0 && location.getY() < 256;
    }

    public static @NotNull BlockCollisionView highestBlockBelow(@NotNull World world, @NotNull WorldBridge bridge,
                                                                @NotNull BoundingBox boundingBox) {
        BoundingBox shrunkenBounds = boundingBox.clone().resize(
                boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMinY() + 1, boundingBox.getMaxZ());

        List<BlockCollisionView> views = new ArrayList<>();
        do {
            shrunkenBounds.shift(0, -1, 0);
            BoundedBlockIterator iterator = new BoundedBlockIterator((x, y, z) ->
                    bridge.collisionFor(world.getBlockAt(x, y, z)), shrunkenBounds);

            while(iterator.hasNext()) {
                BlockCollisionView view = iterator.next();
                if(view.overlaps(shrunkenBounds)) {
                    views.add(view);
                }
            }
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

        return bridge.collisionFor(world.getBlockAt(NumberConversions.floor(boundingBox.getCenterX()),
                NumberConversions.floor(boundingBox.getMinY()), NumberConversions.floor(boundingBox.getCenterZ())));
    }
}

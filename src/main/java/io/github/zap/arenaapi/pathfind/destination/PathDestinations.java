package io.github.zap.arenaapi.pathfind.destination;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProviders;
import io.github.zap.arenaapi.pathfind.path.PathTarget;
import io.github.zap.arenaapi.pathfind.util.Utils;
import io.github.zap.commons.vectors.Vector3I;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class PathDestinations {
    public static @NotNull PathDestination basic(@NotNull PathTarget target, int x, int y, int z) {
        return new PathDestinationImpl(target, x, y, z);
    }

    public static @NotNull PathDestination basic(@NotNull PathTarget target, @NotNull Vector3I vector) {
        return basic(target, vector.x(), vector.y(), vector.z());
    }

    public static @NotNull PathDestination fromEntity(@NotNull Entity entity, @NotNull PathTarget target, boolean findBlock) {
        Location location = entity.getLocation();

        if(Utils.isValidLocation(entity.getLocation())) {
            if(findBlock) {
                BlockCollisionProvider blockCollisionProvider =
                        BlockCollisionProviders.proxyAsyncProvider(entity.getWorld(), 1);

                BoundingBox originalBounds = entity.getBoundingBox();
                BoundingBox shrunkenBounds = entity.getBoundingBox().resize(
                        originalBounds.getMinX(), originalBounds.getMinY(), originalBounds.getMinZ(),
                        originalBounds.getMaxX(), originalBounds.getMinY() + 1, originalBounds.getMaxZ());

                List<BlockCollisionView> views;
                do {
                    shrunkenBounds.shift(0, -1, 0);
                    views = blockCollisionProvider.solidsOverlapping(entity.getBoundingBox());
                }
                while(views.isEmpty() && shrunkenBounds.getMinY() >= 1);

                if(!views.isEmpty()) {
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
                        return new PathDestinationImpl(target, highestView.x(), highestView.collision().isFull() ?
                                highestView.y() + 1 : highestView.y(), highestView.z());
                    }
                }
            }
        }

        return new PathDestinationImpl(target, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static @Nullable PathDestination fromCoordinates(@NotNull PathTarget target, @NotNull World world, double x, double y, double z) {
        if(Utils.isValidLocation(new Location(world, x, y, z))) {
            return new PathDestinationImpl(target, NumberConversions.floor(x), NumberConversions.floor(y),
                    NumberConversions.floor(z));
        }

        return null;
    }

    public static @Nullable PathDestination fromLocation(@NotNull Location source, @NotNull PathTarget target) {
        Objects.requireNonNull(source, "source cannot be null!");
        if(Utils.isValidLocation(source)) {
            return new PathDestinationImpl(target, source.getBlockX(), source.getBlockY(), source.getBlockZ());
        }

        return null;
    }
}

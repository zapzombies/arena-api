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
                BlockCollisionView highest = Utils.highestBlockBelow(BlockCollisionProviders
                        .proxyAsyncProvider(entity.getWorld(), 1), entity.getBoundingBox());

                if(highest != null) {
                    return new PathDestinationImpl(target, highest.x(), highest.collision().isFull() ?
                            highest.y() + 1 : highest.y(), highest.z());
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

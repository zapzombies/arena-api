package io.github.zap.arenaapi.pathfind.agent;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.util.Utils;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vectors;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

/**
 * Contains methods used to produce default {@link PathAgent} implementations.
 */
public final class PathAgents {
    /**
     * Creates a default implementation of PathAgent with the described parameters.
     * @param vector The precise world-space coordinates of the agent
     * @param width The width of the agent (which will be the same for both the X and Z axis)
     * @param height The height of the agent
     * @param jumpHeight The number of blocks this agent is capable of jumping vertically
     * @param fallTolerance The maximum number of blocks this agent should be able to fall
     * @return A new, default implementation of PathAgent
     * @throws NullPointerException if vector is null
     * @throws IllegalArgumentException if any of the provided arguments are invalid
     */
    public static @NotNull PathAgent fromVector(@NotNull Vector3D vector, double width, double height,
                                                double jumpHeight, double fallTolerance) {
        Validate.notNull(vector, "vector cannot be null");
        Validate.isTrue(Vectors.isFinite(vector), "vector must be finite");
        Validate.finite(width, "width must be finite");
        Validate.finite(height, "height must be finite");
        Validate.finite(jumpHeight, "jumpHeight must be finite");
        Validate.isTrue(width > 0, "width must be > 0");
        Validate.isTrue(height > 0, "height must be > 0");
        Validate.isTrue(jumpHeight >= 0, "jumpHeight must be >= 0");
        Validate.isTrue(fallTolerance >= 0, "fallTolerance must be >= 0");
        return new PathAgentImpl(vector.x(), vector.y(), vector.z(), width, height, jumpHeight, fallTolerance);
    }

    public static @NotNull PathAgent fromEntity(@NotNull Entity entity, @NotNull WorldBridge bridge, double jumpHeight, double fallTolerance) {
        while(entity.getVehicle() != null) {
            entity = entity.getVehicle();
        }

        BlockCollisionView view = Utils.highestBlockBelow(entity.getWorld(), bridge, entity.getBoundingBox());
        Location location = entity.getLocation();

        return fromVector(Vectors.of(location.getX(), view.exactY(), location.getZ()), entity.getWidth(),
                entity.getHeight(), jumpHeight, fallTolerance);
    }
}

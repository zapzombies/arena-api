package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.commons.vectors.*;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vector3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NodeStepper {
    @Nullable Vector3I stepDirectional(@NotNull BlockCollisionProvider collisionProvider, @NotNull BlockCollisionView blockAtFeet,
                                       @NotNull PathAgent agent, @NotNull Vector3D position, @NotNull Direction direction);
}

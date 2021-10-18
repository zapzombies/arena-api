package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import org.jetbrains.annotations.NotNull;

public final class NodeExplorers {
    public static @NotNull NodeExplorer basicWalk(@NotNull WorldBridge bridge, @NotNull NodeStepper stepper,
                                                  @NotNull ChunkBounds chunkBounds) {
        return new WalkNodeExplorer(bridge, stepper, chunkBounds);
    }
}

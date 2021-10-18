package io.github.zap.arenaapi.pathfind.process;

import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PostProcessor {
    @NotNull PathNode process(@NotNull PathfinderContext context, @NotNull PathAgent agent, @NotNull PathNode firstNode);
}

package io.github.zap.arenaapi.pathfind.operation;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.agent.PathAgents;
import io.github.zap.arenaapi.pathfind.calculate.*;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.chunk.ChunkCoordinateProviders;
import io.github.zap.arenaapi.pathfind.destination.PathDestination;
import io.github.zap.arenaapi.pathfind.destination.PathDestinations;
import io.github.zap.arenaapi.pathfind.path.PathTarget;
import io.github.zap.arenaapi.pathfind.process.PostProcessor;
import io.github.zap.arenaapi.pathfind.step.NodeExplorer;
import io.github.zap.arenaapi.pathfind.step.NodeExplorers;
import io.github.zap.arenaapi.pathfind.step.NodeStepper;
import io.github.zap.arenaapi.pathfind.step.NodeSteppers;
import io.github.zap.commons.vectors.Vector3I;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathOperationBuilder {
    private static final int DEFAULT_PATHFIND_RADIUS = 3;
    private static final double DEFAULT_JUMP_HEIGHT = 1.125;
    private static final double DEFAULT_FALL_TOLERANCE = 16;

    private final WorldBridge bridge;

    private final PathAgent agent;
    private final Entity agentEntity;
    private final PathDestination destination;

    private final List<PostProcessor> postProcessors = new ArrayList<>();

    private double jumpHeight = DEFAULT_JUMP_HEIGHT;
    private double fallTolerance = DEFAULT_FALL_TOLERANCE;

    private HeuristicCalculator heuristicCalculator;
    private AversionCalculator aversionCalculator;
    private SuccessCondition successCondition;
    private NodeExplorer nodeExplorer;
    private ChunkBounds chunkBounds;
    private NodeStepper nodeStepper;


    private int pathfindRadius = DEFAULT_PATHFIND_RADIUS;

    public PathOperationBuilder(@NotNull WorldBridge bridge, @NotNull PathAgent agent,
                                @NotNull PathDestination destination) {
        this.bridge = Objects.requireNonNull(bridge, "bridge cannot be null");
        this.agent = Objects.requireNonNull(agent, "agent cannot be null");
        this.agentEntity = null;
        this.destination = Objects.requireNonNull(destination, "destination cannot be null");
    }

    public PathOperationBuilder(@NotNull WorldBridge bridge, @NotNull Entity agentEntity,
                                @NotNull PathDestination destination) {
        this.bridge = Objects.requireNonNull(bridge, "bridge cannot be null");
        this.agent = null;
        this.agentEntity = Objects.requireNonNull(agentEntity, "agentEntity cannot be null");
        this.destination = Objects.requireNonNull(destination, "destination cannot be null");
    }

    public @NotNull PathOperationBuilder withJumpHeight(double jumpHeight) {
        this.jumpHeight = jumpHeight;
        return this;
    }

    public @NotNull PathOperationBuilder withFallTolerance(double fallTolerance) {
        this.fallTolerance = fallTolerance;
        return this;
    }

    public @NotNull PathOperationBuilder withHeuristic(@Nullable HeuristicCalculator heuristicCalculator) {
        this.heuristicCalculator = heuristicCalculator;
        return this;
    }

    public @NotNull PathOperationBuilder withAversion(@NotNull AversionCalculator aversionCalculator) {
        this.aversionCalculator = aversionCalculator;
        return this;
    }

    public @NotNull PathOperationBuilder withSuccessCondition(@NotNull SuccessCondition successCondition) {
        this.successCondition = successCondition;
        return this;
    }

    public @NotNull PathOperationBuilder withExplorer(@Nullable NodeExplorer nodeExplorer) {
        this.nodeExplorer = nodeExplorer;
        return this;
    }

    public @NotNull PathOperationBuilder withRange(@Nullable ChunkBounds chunkBounds) {
        this.chunkBounds = chunkBounds;
        return this;
    }

    public @NotNull PathOperationBuilder withRange(int pathfindRadius) {
        this.pathfindRadius = pathfindRadius;
        return this;
    }

    public @NotNull PathOperationBuilder withStepper(@Nullable NodeStepper nodeStepper) {
        this.nodeStepper = nodeStepper;
        return this;
    }

    public @NotNull PathOperationBuilder addPostProcessor(@NotNull PostProcessor postProcessor) {
        this.postProcessors.add(Objects.requireNonNull(postProcessor, "postProcessor cannot be null"));
        return this;
    }

    public @NotNull PathOperation build() {
        Objects.requireNonNull(bridge, "Must specify a bridge!");
        Objects.requireNonNull(destination, "Must specify a destination!");

        heuristicCalculator = heuristicCalculator == null ? HeuristicCalculators.distanceOnly() : heuristicCalculator;
        aversionCalculator = aversionCalculator == null ? AversionCalculators.defaultWalk() : aversionCalculator;
        successCondition = successCondition == null ? SuccessConditions.sameBlock() : successCondition;

        //both this.agent and this.agentEntity cannot both be null, hence suppression
        //noinspection ConstantConditions
        PathAgent agent = this.agent == null ? PathAgents.fromEntity(agentEntity, jumpHeight, fallTolerance) : this.agent;

        chunkBounds = chunkBounds == null ?
                ChunkCoordinateProviders.squareFromCenter(Vectors.asChunk(agent), pathfindRadius) : chunkBounds;
        nodeExplorer = nodeExplorer == null ? NodeExplorers.basicWalk(bridge, nodeStepper == null ?
                NodeSteppers.basicWalk() : nodeStepper, chunkBounds) : nodeExplorer;

        return new PathOperationImpl(agent, destination, heuristicCalculator, aversionCalculator, successCondition,
                nodeExplorer, chunkBounds, postProcessors);
    }
}

package io.github.zap.arenaapi.pathfind.context;

import io.github.zap.arenaapi.pathfind.process.PathMerger;
import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AsyncPathfinderContext implements PathfinderContext {
    protected final Queue<PathResult> successfulPaths = new ArrayDeque<>();
    protected final Queue<PathResult> failedPaths = new ArrayDeque<>();

    protected final ExecutorService executor;
    protected final BlockCollisionProvider blockCollisionProvider;
    protected final PathMerger merger;
    protected final int pathCapacity;

    public AsyncPathfinderContext(@NotNull ExecutorService executor, @NotNull BlockCollisionProvider blockCollisionProvider,
                                  @NotNull PathMerger merger, int pathCapacity) {
        this.executor = executor;
        this.blockCollisionProvider = blockCollisionProvider;
        this.merger = merger;
        this.pathCapacity = pathCapacity;
    }

    @Override
    public void recordPath(@NotNull PathResult result) {}

    @Override
    public @NotNull BlockCollisionProvider blockProvider() {
        return blockCollisionProvider;
    }

    @Override
    public @NotNull PathMerger merger() {
        return merger;
    }

    @Override
    public @NotNull Collection<PathResult> failedPaths() {
        return failedPaths;
    }

    @Override
    public @NotNull Collection<PathResult> successfulPaths() {
        return successfulPaths;
    }

    @Override
    public String toString() {
        return "AsyncPathfinderContext{blockCollisionProvider=" + blockCollisionProvider + "}";
    }

    public @NotNull ExecutorService executor() {
        return executor;
    }
}
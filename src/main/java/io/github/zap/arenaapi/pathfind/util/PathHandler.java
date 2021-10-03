package io.github.zap.arenaapi.pathfind.util;

import io.github.zap.arenaapi.pathfind.path.PathResult;
import io.github.zap.arenaapi.pathfind.engine.PathfinderEngine;
import io.github.zap.arenaapi.pathfind.operation.PathOperation;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Utility meant to simplify access to PathfinderEngines, particularly asynchronous ones. Prevents redundant
 * PathOperations from being queued (if a PathOperation is already in progress, it will not be queued). Furthermore,
 * PathOperations cannot be queued after the PathResult is available until the result has been retrieved. Redundant
 * or unnecessary calls to tryTakeResult are both performant and safe; they will simply return null unless there is
 * a valid value.
 */
public class PathHandler {
    private final PathfinderEngine engine;
    private Future<PathResult> result;

    public PathHandler(@NotNull PathfinderEngine engine) {
        this.engine = engine;
    }

    public void giveOperation(@NotNull PathOperation operation, @NotNull World world) {
        if(result == null) {
            result = engine.giveOperation(operation, world);
        }
    }

    public @Nullable PathResult tryTakeResult() {
        if(result != null && result.isDone()) {
            if(result.isCancelled()) {
                result = null;
                return null;
            }
            else if(result.isDone()) {
                try {
                    PathResult value = result.get();
                    result = null;
                    return value;
                } catch (InterruptedException | ExecutionException exception) {
                    engine.getPlugin().getLogger().log(Level.WARNING, "exception thrown when retrieving a " +
                            "completed PathResult", exception);
                }
            }
        }

        return null;
    }

    public boolean isRunning() {
        if(result != null) {
            return !result.isDone();
        }

        return false;
    }
}

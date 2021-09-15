package io.github.zap.arenaapi.nms.common.pathfind;

import org.jetbrains.annotations.Nullable;

public interface PathEntityWrapper {
    int pathLength();

    boolean reachesDestination();

    boolean hasFinished();

    @Nullable PathPointWrapper lastPoint();
}

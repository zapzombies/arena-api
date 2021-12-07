package io.github.zap.arenaapi.nms.v1_16_R3.pathfind;

import io.github.zap.arenaapi.nms.common.pathfind.PathEntityWrapper;
import io.github.zap.arenaapi.nms.common.pathfind.PathPointWrapper;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PathEntityWrapper_v1_16_R3(PathEntity pathEntity) implements PathEntityWrapper {
    @Override
    public int pathLength() {
        return pathEntity.e();
    }

    @Override
    public boolean reachesDestination() {
        return pathEntity.j();
    }

    @Override
    public boolean hasFinished() {
        return pathEntity.c();
    }

    @Override
    public @Nullable PathPointWrapper lastPoint() {
        PathPoint last = pathEntity.d();
        return last == null ? null : new PathPointWrapper_v1_16_R3(last);
    }

    @Override
    public void addPoint(int x, int y, int z) {
        pathEntity.getPoints().add(new PathPoint(x, y, z));
    }

    public @NotNull PathEntity pathEntity() {
        return pathEntity;
    }
}

package io.github.zap.arenaapi.pathfind.agent;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

record PathAgentImpl(double x, double y, double z, double width, double height, double jumpHeight,
                     double fallTolerance) implements PathAgent {
    @Override
    public String toString() {
        return "PathAgentImpl{x=" + x + ", y=" + y + ", z=" + z + ", width=" + width + ", height=" + height +
                ", jumpHeight=" + jumpHeight + ", fallTolerance=" + fallTolerance + "}";
    }

    @Override
    public @NotNull BoundingBox getBounds() {
        double halfWidth = width / 2;
        return new BoundingBox(x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth);
    }
}

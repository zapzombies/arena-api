package io.github.zap.arenaapi.pathfind.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static boolean isValidLocation(@NotNull Location location) {
        return location.getWorld().getWorldBorder().isInside(location) && location.getY() >= 0 && location.getY() < 256;
    }
}

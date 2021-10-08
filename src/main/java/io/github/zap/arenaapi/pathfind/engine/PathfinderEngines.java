package io.github.zap.arenaapi.pathfind.engine;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PathfinderEngines {
    public static @NotNull PathfinderEngine proxyAsync(@NotNull Plugin plugin) {
        return new AsyncProxyPathfinderEngine(plugin);
    }
}
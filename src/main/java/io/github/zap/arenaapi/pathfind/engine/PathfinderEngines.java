package io.github.zap.arenaapi.pathfind.engine;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PathfinderEngines {
    public static @NotNull PathfinderEngine proxyAsync(@NotNull Plugin plugin, @NotNull WorldBridge bridge) {
        return new AsyncProxyPathfinderEngine(plugin, bridge);
    }
}
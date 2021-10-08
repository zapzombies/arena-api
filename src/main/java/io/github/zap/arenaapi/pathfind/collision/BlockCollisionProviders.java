package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class BlockCollisionProviders {
    public static @NotNull BlockCollisionProvider proxyAsyncProvider(@NotNull Plugin plugin,
                                                                     @NotNull WorldBridge bridge, @NotNull World world) {
        return new ProxyBlockCollisionProvider(plugin, bridge, world);
    }
}

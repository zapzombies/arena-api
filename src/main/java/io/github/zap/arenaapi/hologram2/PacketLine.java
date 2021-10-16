package io.github.zap.arenaapi.hologram2;

import io.github.zap.arenaapi.nms.common.packet.Packet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PacketLine<V> extends HologramLine<V> {

    private final Packet spawnPacket;

    private final Function<@NotNull V, @NotNull Packet> updatePacket;

    private final Packet destructionPacket;

    public PacketLine(@NotNull Packet spawnPacket, @NotNull Function<@NotNull V, @NotNull Packet> updatePacket,
                      @NotNull Packet destructionPacket, @NotNull V defaultVisual) {
        super(defaultVisual);

        this.spawnPacket = spawnPacket;
        this.updatePacket = updatePacket;
        this.destructionPacket = destructionPacket;
    }

    @Override
    public void createVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        spawnPacket.sendToPlayer(plugin, player);
    }

    @Override
    public void updateVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        updatePacket.apply(getVisualForPlayer(player)).sendToPlayer(plugin, player);
    }

    @Override
    public void destroyVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        destructionPacket.sendToPlayer(plugin, player);
    }

}

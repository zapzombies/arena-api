package io.github.zap.arenaapi.nms.common.packet;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * A packet using {@link PacketContainer}s
 */
@SuppressWarnings("ClassCanBeRecord")
public class ProtocolLibPacket implements Packet {

    private final ProtocolManager protocolManager;

    private final PacketContainer handle;

    public ProtocolLibPacket(@NotNull ProtocolManager protocolManager, @NotNull PacketContainer handle) {
        this.protocolManager = protocolManager;
        this.handle = handle;
    }

    @Override
    public void sendToPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        try {
            protocolManager.sendServerPacket(player, handle);
        } catch (InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to send a packet to player with UUID "
                    + player.getUniqueId() + "!", e);
        }
    }

}

package io.github.zap.arenaapi.nms.common.packet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A minecraft packet used to circumvent limitations of the Bukkit API
 */
public interface Packet {

    /**
     * Sends the packet to a {@link Player}.
     * @param plugin The {@link Plugin} from which the packet is being sent
     * @param player The {@link Player} to send the packet to
     */
    void sendToPlayer(@NotNull Plugin plugin, @NotNull Player player);

}

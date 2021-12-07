package io.github.zap.arenaapi.nms.common.packet;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A bridge used for creating and interacting with packets
 */
public interface PacketBridge {

    @NotNull Packet createSpawnLivingEntityPacket(int entityId, int typeId, @NotNull UUID uuid, double x, double y,
                                                  double z);

    @NotNull Packet createHologramLinePacket(int entityId, @NotNull Component line);

    @NotNull Packet createDestroyEntityPacket(int id);

    @NotNull Packet createDestroyEntitiesPacket(int[] ids);
}

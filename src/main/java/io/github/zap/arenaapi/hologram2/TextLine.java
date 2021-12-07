package io.github.zap.arenaapi.hologram2;

import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.packet.Packet;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class TextLine extends PacketLine<Component> {

    private TextLine(@NotNull Packet spawnPacket, @NotNull Function<@NotNull Component, @NotNull Packet> updatePacket,
                     @NotNull Packet destroyPacket) {
        super(spawnPacket, updatePacket, destroyPacket, Component.empty());
    }

    public static @NotNull TextLine textLine(@NotNull EntityBridge entityBridge, @NotNull PacketBridge packetBridge,
                                             @NotNull Location location) {
        int entityId = entityBridge.nextEntityID();
        Packet spawnPacket = packetBridge.createSpawnLivingEntityPacket(entityId,
                entityBridge.getEntityTypeID(EntityType.ARMOR_STAND), entityBridge.randomUUID(), location.getX(),
                location.getY(), location.getZ());
        Function<@NotNull Component, @NotNull Packet> updatePacket
                = component -> packetBridge.createHologramLinePacket(entityId, component);
        Packet destroyPacket = packetBridge.createDestroyEntityPacket(entityId);

        return new TextLine(spawnPacket, updatePacket, destroyPacket);
    }

}

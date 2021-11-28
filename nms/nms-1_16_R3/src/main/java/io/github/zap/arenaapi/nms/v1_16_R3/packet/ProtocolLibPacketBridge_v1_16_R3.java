package io.github.zap.arenaapi.nms.v1_16_R3.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AdventureComponentConverter;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.zap.arenaapi.nms.common.packet.Packet;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
import io.github.zap.arenaapi.nms.common.packet.ProtocolLibPacket;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class ProtocolLibPacketBridge_v1_16_R3 implements PacketBridge {

    private final static byte INVISIBLE_BYTE_MASK = (byte) 0x20;

    private final static byte MARKER_ARMOR_STAND_MASK = (byte) 0x10;

    protected final ProtocolManager protocolManager;

    public ProtocolLibPacketBridge_v1_16_R3(@NotNull ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    @Override
    public @NotNull Packet createSpawnLivingEntityPacket(int entityId, int typeId, @NotNull UUID uuid, double x,
                                                         double y, double z) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packetContainer.getIntegers()
                .write(0, entityId)
                .write(1,typeId);
        packetContainer.getUUIDs().write(0, uuid);
        packetContainer.getDoubles()
                .write(0, x)
                .write(1, y)
                .write(2, z);

        return createProtocolLibPacket(packetContainer);
    }

    @Override
    public @NotNull Packet createHologramLinePacket(int entityId, @NotNull Component line) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, entityId);

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.Serializer optChatSerializer = WrappedDataWatcher.Registry
                .getChatComponentSerializer(true);
        WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

        WrappedDataWatcher.WrappedDataWatcherObject invisible
                = new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer);
        WrappedDataWatcher.WrappedDataWatcherObject customName
                = new WrappedDataWatcher.WrappedDataWatcherObject(2, optChatSerializer);
        WrappedDataWatcher.WrappedDataWatcherObject customNameVisible
                = new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer);
        WrappedDataWatcher.WrappedDataWatcherObject marker
                = new WrappedDataWatcher.WrappedDataWatcherObject(14, byteSerializer);

        wrappedDataWatcher.setObject(invisible, INVISIBLE_BYTE_MASK);
        wrappedDataWatcher.setObject(customName, Optional.of(AdventureComponentConverter.fromComponent(line)));
        wrappedDataWatcher.setObject(customNameVisible, true);
        wrappedDataWatcher.setObject(marker, MARKER_ARMOR_STAND_MASK);

        packetContainer.getWatchableCollectionModifier().write(0, wrappedDataWatcher.getWatchableObjects());

        return createProtocolLibPacket(packetContainer);
    }

    @Override
    public @NotNull Packet createDestroyEntityPacket(int id) {
        return createDestroyEntitiesPacket(new int[] { id });
    }

    @Override
    public @NotNull Packet createDestroyEntitiesPacket(int[] ids) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, ids);

        return createProtocolLibPacket(packetContainer);
    }

    protected @NotNull Packet createProtocolLibPacket(@NotNull PacketContainer packetContainer) {
        return new ProtocolLibPacket(protocolManager, packetContainer);
    }

}

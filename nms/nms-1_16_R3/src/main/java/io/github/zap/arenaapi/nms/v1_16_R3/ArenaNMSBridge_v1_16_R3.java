package io.github.zap.arenaapi.nms.v1_16_R3;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.zap.arenaapi.nms.common.ArenaNMSBridge;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.itemstack.ItemStackBridge;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
import io.github.zap.arenaapi.nms.common.player.PlayerBridge;
import io.github.zap.arenaapi.nms.common.world.WorldBridge;
import io.github.zap.arenaapi.nms.v1_16_R3.entity.EntityBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.itemstack.ItemStackBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.packet.PacketBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.player.PlayerBridge_v1_16_R3;
import io.github.zap.arenaapi.nms.v1_16_R3.world.WorldBridge_v1_16_R3;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public class ArenaNMSBridge_v1_16_R3 implements ArenaNMSBridge {
    public static final ArenaNMSBridge_v1_16_R3 INSTANCE = new ArenaNMSBridge_v1_16_R3();
    private static final String VERSION = "v1_16_R3";

    private final PacketBridge packetBridge;

    private ArenaNMSBridge_v1_16_R3() {
        try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary");
        }
        catch (ClassNotFoundException error) {
            throw new NotImplementedException("No minecraft PacketBridge implementation exists for version " +
                    VERSION + ", and ProtocolLibrary was not detected on the server!");
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        this.packetBridge = new PacketBridge_v1_16_R3(protocolManager);
    }

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return EntityBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull ItemStackBridge itemStackBridge() {
        return ItemStackBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull PacketBridge packetBridge() {
        return packetBridge;
    }

    @Override
    public @NotNull PlayerBridge playerBridge() {
        return PlayerBridge_v1_16_R3.INSTANCE;
    }

    @Override
    public @NotNull WorldBridge worldBridge() {
        return WorldBridge_v1_16_R3.INSTANCE;
    }
}

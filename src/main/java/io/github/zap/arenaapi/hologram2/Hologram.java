package io.github.zap.arenaapi.hologram2;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.packet.Packet;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hologram which can show lines of text or items
 */
public class Hologram {

    public static final double DEFAULT_LINE_SPACE = 0.25;

    private final Plugin plugin;

    private final PacketBridge packetBridge;

    @Getter
    private final List<HologramLine<?>> hologramLines = new ArrayList<>();

    private final double lineSpace;

    private final Location rootLocation;

    public Hologram(@NotNull Plugin plugin, @NotNull PacketBridge packetBridge, @NotNull Location location,
                    double lineSpace) {
        this.plugin = plugin;
        this.packetBridge = packetBridge;
        this.rootLocation = location;
        this.lineSpace = lineSpace;
    }

    public Hologram(@NotNull ArenaApi arenaApi, @NotNull Location location) {
        this(arenaApi, arenaApi.getNmsBridge().packetBridge(), location, DEFAULT_LINE_SPACE);
    }

    @Deprecated
    public Hologram(@NotNull Location location, double lineSpace) {
        this(ArenaApi.getInstance(), ArenaApi.getInstance().getNmsBridge().packetBridge(), location, lineSpace);
    }

    @Deprecated
    public Hologram(@NotNull Location location) {
        this(location, DEFAULT_LINE_SPACE);
    }

    /**
     * Adds a line with a message key and format arguments
     * @param message A pair of the message key and format arguments
     */
    public void addLine(Component message) {
        PacketLine textLine = createTextLine(rootLocation.clone().subtract(0, lineSpace * hologramLines.size(),
                0), message);
        hologramLines.add(textLine);
    }

    private PacketLine createTextLine(Location location, Component message) {
        PacketLine textLine = new PacketLine(location);
        textLine.setVisualForEveryone(message);

        return textLine;
    }

    /**
     * Updates a text line for all players and overrides custom visuals
     * @param index The index of the line to update
     * @param message The updated line
     */
    public void updateLineForEveryone(int index, Component message) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof PacketLine textLine) {
            textLine.setVisualForEveryone(message);
        } else {

        }
    }

    /**
     * Updates a text line for all players
     * @param index The index of the line to update
     * @param message The updated line
     */
    public void updateLine(int index, Component message) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof PacketLine textLine) {
            textLine.setVisual(message);
        } else {

        }
    }

    /**
     * Updates a text line for a single player
     * @param player The player to update the line for
     * @param index The index of the line to update
     * @param message The updated line
     */
    public void updateLineForPlayer(Player player, int index, Component message) {

    }

    /**
     * Creates and renders the hologram for a player
     * @param player The player to render the hologram to
     */
    public void renderToPlayer(Player player) {
        for (HologramLine<?> hologramLine : hologramLines) {
            hologramLine.createVisualForPlayer(plugin, player);
            hologramLine.updateVisualForPlayer(player);
        }
    }

    /**
     * Destroys the hologram
     */
    public void destroy() {
        int idCount = hologramLines.size();

        int[] ids = new int[idCount];
        for (int i = 0; i < idCount; i++) {
            ids[i] = hologramLines.get(0).getEntityId();
            hologramLines.remove(0);
        }

        Packet packet = packetBridge.createDestroyEntitiesPacket(ids);
        for (Player player : rootLocation.getWorld().getPlayers()) {
            packet.sendToPlayer(plugin, player);
        }
    }

}

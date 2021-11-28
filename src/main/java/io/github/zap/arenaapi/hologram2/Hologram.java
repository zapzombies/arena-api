package io.github.zap.arenaapi.hologram2;

import io.github.zap.arenaapi.ArenaApi;
import io.github.zap.arenaapi.nms.common.entity.EntityBridge;
import io.github.zap.arenaapi.nms.common.packet.PacketBridge;
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

    private final EntityBridge entityBridge;

    private final PacketBridge packetBridge;

    private final List<HologramLine<?>> hologramLines = new ArrayList<>();

    private final double lineSpace;

    private final Location rootLocation;

    public Hologram(@NotNull Plugin plugin, @NotNull EntityBridge entityBridge, @NotNull PacketBridge packetBridge,
                    @NotNull Location location, double lineSpace) {
        this.plugin = plugin;
        this.entityBridge = entityBridge;
        this.packetBridge = packetBridge;
        this.rootLocation = location;
        this.lineSpace = lineSpace;
    }

    public Hologram(@NotNull ArenaApi arenaApi, @NotNull Location location) {
        this(arenaApi, arenaApi.getNmsBridge().entityBridge(), arenaApi.getNmsBridge().packetBridge(), location,
                DEFAULT_LINE_SPACE);
    }

    @Deprecated
    public Hologram(@NotNull Location location, double lineSpace) {
        this(ArenaApi.getInstance(), ArenaApi.getInstance().getNmsBridge().entityBridge(),
                ArenaApi.getInstance().getNmsBridge().packetBridge(), location, lineSpace);
    }

    @Deprecated
    public Hologram(@NotNull Location location) {
        this(location, DEFAULT_LINE_SPACE);
    }

    /**
     * Adds a line with a message key and format arguments
     * @param message A pair of the message key and format arguments
     */
    public void addLine(@NotNull Component message) {
        PacketLine<Component> textLine = createTextLine(rootLocation.clone().subtract(0,
                lineSpace * hologramLines.size(), 0), message);
        hologramLines.add(textLine);
    }

    private @NotNull PacketLine<Component> createTextLine(@NotNull Location location, @NotNull Component message) {

        return TextLine.textLine(entityBridge, packetBridge, location);
    }

    /**
     * Updates a text line for all players and overrides custom visuals
     * @param index The index of the line to update
     * @param message The updated line
     */
    public void updateLineForEveryone(int index, @NotNull Component message) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof TextLine textLine) {
            for (Player player : rootLocation.getWorld().getPlayers()) {
                textLine.setVisualForPlayer(plugin, player, message);
            }
        } else {

        }
    }

    /**
     * Updates a text line for all players
     * @param index The index of the line to update
     * @param message The updated line
     */
    public void updateLine(int index, @NotNull Component message) {
        HologramLine<?> hologramLine = hologramLines.get(index);
        if (hologramLine instanceof TextLine textLine) {
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
    public void renderToPlayer(@NotNull Player player) {
        for (HologramLine<?> hologramLine : hologramLines) {
            hologramLine.createVisualForPlayer(plugin, player);
            hologramLine.updateVisualForPlayer(plugin, player);
        }
    }

    /**
     * Destroys the hologram
     */
    public void destroy() {
        while (!hologramLines.isEmpty()) {
            HologramLine<?> line = hologramLines.remove(0);
            for (Player player : rootLocation.getWorld().getPlayers()) {
                line.destroyVisualForPlayer(plugin, player);
            }
        }
    }

    public @NotNull List<HologramLine<?>> getHologramLines() {
        return hologramLines;
    }

}

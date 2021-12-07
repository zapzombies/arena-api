package io.github.zap.arenaapi.hologram2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * A line of a hologram
 */
public abstract class HologramLine<V> {

    private final Map<UUID, V> visualMap = new HashMap<>();

    private V defaultVisual;

    public HologramLine(@NotNull V defaultVisual) {
        this.defaultVisual = defaultVisual;
    }

    /**
     * Sets the visual of the hologram for all players, overriding player specific visuals
     * @param plugin The {@link Plugin} that cleared the visuals
     */
    public void clearVisuals(@NotNull Plugin plugin) {
        Iterator<UUID> viewers = visualMap.keySet().iterator();
        while (viewers.hasNext()) {
            UUID viewer = viewers.next();
            Player player = Bukkit.getPlayer(viewer);

            if (player != null) {
                destroyVisualForPlayer(plugin, player);
            }

            viewers.remove();
        }
    }

    /**
     * Sets the default visual of the hologram for all players
     * @param visual The new visual
     */
    public void setVisual(@NotNull V visual) {
        defaultVisual = visual;
    }

    /**
     * Sets the visual for a single player
     * @param plugin The {@link Plugin} to set the visual from
     * @param player The player to set the visual for
     * @param visual The new visual
     */
    public void setVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player, @NotNull V visual) {
        visualMap.put(player.getUniqueId(), visual);
        updateVisualForPlayer(plugin, player);
    }

    /**
     * Gets the visual for a player
     * @param player The player to get the visual for
     * @return The visual the player sees
     */
    public @NotNull V getVisualForPlayer(@NotNull Player player) {
        return visualMap.getOrDefault(player.getUniqueId(), defaultVisual);
    }
    /**
     * Spawns the visual for a player
     * @param plugin The {@link Plugin} to send the visual from
     * @param player The player to spawn the visual for
     */
    public abstract void createVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player);

    /**
     * Updates the visual for a player
     * @param plugin The {@link Plugin} to update the visual from
     * @param player The player to update for
     */
    public abstract void updateVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player);

    public abstract void destroyVisualForPlayer(@NotNull Plugin plugin, @NotNull Player player);

}

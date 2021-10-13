package io.github.zap.arenaapi.world;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface WorldLoader {
    /**
     * Preloads all necessary worlds for this implementation.
     */
    void preload();

    /**
     * Loads the map associated with worldName. This should copy from worlds cached via preloadWorlds, if possible.
     * Implementations of this function may run fully or partially async.
     * @param worldName The name of the world to load from
     */
    @NotNull CompletableFuture<World> loadWorld(String worldName);

    /**
     * Unloads the specified world.
     * @param world The world to unload
     */
    boolean unloadWorld(World world);

    /**
     * Determine if the specified world exists. It may not be loaded.
     * @param worldName The world to test
     * @return Whether or not the world exists
     */
    boolean worldExists(String worldName);
}
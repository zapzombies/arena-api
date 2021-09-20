package io.github.zap.arenaapi.game.player;

import io.github.zap.arenaapi.Keyed;
import io.github.zap.arenaapi.ReloadablePlugin;
import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

public abstract class ArenaPlayer<PlayerType extends ArenaPlayer<PlayerType>> implements PlayerView {
    protected final ReloadablePlugin<?> playerPlugin;
    protected final UUID playerUUID;

    private final Map<String, StateOperation> stateMap;
    private Reference<Player> playerCache;
    private StateOperation currentState;

    protected ArenaPlayer(@NotNull ReloadablePlugin<?> plugin, @NotNull UUID playerUUID) {
        this.playerPlugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.playerUUID = Objects.requireNonNull(playerUUID, "playerUUID cannot be null");
        this.stateMap = new HashMap<>();
        this.playerCache = new WeakReference<>(null);
        this.currentState = null;
    }

    @Override
    public @NotNull Optional<Player> getPlayerIfValid() {
        if(isPlayerValid()) {
            Player cached = playerCache.get();

            if(cached == null) {
                Player player = playerPlugin.getServer().getPlayer(playerUUID);
                if(player != null) {
                    playerCache = new WeakReference<>(player);
                    return Optional.of(player);
                }
            }
            else if(cached.isOnline()) {
                return Optional.of(cached);
            }
            else {
                playerCache.clear();
            }
        }
        else {
            playerCache.clear();
        }

        return Optional.empty();
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return playerPlugin.getServer().getOfflinePlayer(playerUUID);
    }

    @Override
    public @NotNull UUID getUUID() {
        return playerUUID;
    }

    public @NotNull ReloadablePlugin<?> getPlugin() {
        return playerPlugin;
    }

    public void registerState(@NotNull Keyed key, @NotNull StateOperation stateOperation) {
        if(stateMap.putIfAbsent(key.getKey(), stateOperation) != null) {
            throw new IllegalArgumentException("state with key " + key + " already registered");
        }
    }

    public void applyState(@NotNull Keyed state) {
        StateOperation newState = stateMap.get(state.getKey());
        if(newState != null) {
            Optional<Player> playerOptional = getPlayerIfValid();
            if(playerOptional.isPresent()) {
                Player player = playerOptional.get();
                if(currentState != null) {
                    currentState.reverse(player);
                }

                newState.apply(player);
                currentState = newState;
            }
        }
        else {
            throw new IllegalArgumentException("state with key " + state + " not registered for ArenaPlayer " + this);
        }
    }

    public abstract @NotNull PlayerType getArenaPlayer();

    public abstract boolean isPlayerValid();
}
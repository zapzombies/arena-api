package io.github.zap.arenaapi.game.player;

import io.github.zap.arenaapi.Keyed;
import io.github.zap.arenaapi.ReloadablePlugin;
import io.github.zap.arenaapi.hotbar2.PlayerView;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class ArenaPlayer<PlayerType extends ArenaPlayer<PlayerType>> implements PlayerView {

    protected final ReloadablePlugin<?> playerPlugin;
    protected final PlayerView playerView;

    private final Map<String, StateOperation> stateMap = new HashMap<>();
    private StateOperation currentState = null;

    protected ArenaPlayer(@NotNull ReloadablePlugin<?> plugin, @NotNull PlayerView playerView) {
        this.playerPlugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.playerView = Objects.requireNonNull(playerView, "playerView cannot be null");
    }

    @Override
    public @NotNull Optional<Player> getPlayerIfValid() {
        if (isPlayerValid()) {
            playerView.getPlayerIfValid();
        }

        return Optional.empty();
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return playerView.getOfflinePlayer();
    }

    @Override
    public @NotNull UUID getUUID() {
        return playerView.getUUID();
    }

    public @NotNull ReloadablePlugin<?> getPlugin() {
        return playerPlugin;
    }

    public void registerState(@NotNull Keyed key, @NotNull StateOperation stateOperation) {
        if (stateMap.putIfAbsent(key.getKey(), stateOperation) != null) {
            throw new IllegalArgumentException("state with key " + key + " already registered");
        }
    }

    public void applyState(@NotNull Keyed state) {
        StateOperation newState = stateMap.get(state.getKey());
        if (newState != null) {
            Optional<Player> playerOptional = getPlayerIfValid();
            if (playerOptional.isPresent()) {
                Player player = playerOptional.get();
                if (currentState != null) {
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
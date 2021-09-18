package io.github.zap.arenaapi.game.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface StateOperation {
    void apply(@NotNull Player player);

    void reverse(@NotNull Player player);
}

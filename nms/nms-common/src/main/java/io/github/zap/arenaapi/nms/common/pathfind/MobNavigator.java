package io.github.zap.arenaapi.nms.common.pathfind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MobNavigator {
    void navigateAlongPath(@NotNull PathEntityWrapper pathEntityWrapper, double speed);

    @Nullable PathEntityWrapper currentPath();
}

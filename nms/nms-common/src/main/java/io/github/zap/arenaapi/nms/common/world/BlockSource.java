package io.github.zap.arenaapi.nms.common.world;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BlockSource {
    @Nullable BlockCollisionView getBlock(int x, int y, int z);
}

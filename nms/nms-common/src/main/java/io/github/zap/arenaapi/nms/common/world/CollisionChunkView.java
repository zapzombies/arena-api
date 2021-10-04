package io.github.zap.arenaapi.nms.common.world;

import io.github.zap.commons.vectors.Vector2I;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CollisionChunkView extends BlockSource {
    boolean collidesWithAny(@NotNull BoundingBox worldRelativeBounds);

    @NotNull List<BlockCollisionView> collisionsWith(@NotNull BoundingBox worldRelativeBounds);

    @NotNull Vector2I position();

    int captureTick();
}

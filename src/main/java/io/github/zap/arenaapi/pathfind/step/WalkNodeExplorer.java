package io.github.zap.arenaapi.pathfind.step;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.path.PathNode;
import io.github.zap.arenaapi.pathfind.path.PathNodeFactory;
import io.github.zap.arenaapi.pathfind.context.PathfinderContext;
import io.github.zap.commons.vectors.*;
import org.jetbrains.annotations.NotNull;

import java.awt.image.DirectColorModel;

@SuppressWarnings("ClassCanBeRecord") //unintelliJ
class WalkNodeExplorer implements NodeExplorer {
    private final NodeStepper stepper;
    private final ChunkBounds chunkBounds;

    WalkNodeExplorer(@NotNull NodeStepper stepper, @NotNull ChunkBounds chunkBounds) {
        this.stepper = stepper;
        this.chunkBounds = chunkBounds;
    }

    public <T extends PathNode> void exploreNodes(@NotNull PathfinderContext context, @NotNull PathAgent agent,
                                                  T[] buffer, @NotNull T current,
                                                  @NotNull PathNodeFactory<T> pathNodeFactory) {
        if(buffer == null) {
            return;
        }

        BlockCollisionView currentBlock = context.blockProvider().getBlock(current);

        if(currentBlock == null) { //return if we have no block at the current node
            if(buffer.length > 0) {
                buffer[0] = null;
            }

            return;
        }

        Vector3D position;
        boolean isFirst;
        if(Vectors.equals(Vectors.asIntFloor(agent), current)) { //use exact agent position for first node...
            position = agent;
            isFirst = true;
        }
        else { //...otherwise, make the assumption it's trying to pathfind from the exact center of the block
            position = Vectors.of(current.x() + 0.5, currentBlock.exactY(), current.z() + 0.5);
            isFirst = false;
        }

        int j = 0;
        for(int i = 0; i < buffer.length; i++) {
            Direction direction = Direction.valueAtIndex(i);
            if(direction == Direction.UP && currentBlock.collision().isEmpty()) {
                continue;
            }

            Vector3I nextTarget = Vectors.add(current, direction);

            if(chunkBounds.hasBlock(nextTarget)) {
                Vector3I nodePosition = stepper.stepDirectional(context.blockProvider(), agent, position, direction,
                        isFirst);

                if(nodePosition != null && chunkBounds.hasBlock(nodePosition)) {
                    T newNode = pathNodeFactory.make(nodePosition);

                    //exists to account for mojang's buggy code
                    if(currentBlock.collision().isPartial() && nodePosition.y() > position.y()) {
                        newNode.setOffsetVector(Direction.UP);
                    }

                    buffer[j++] = newNode;
                }
            }
        }

        if(j < buffer.length) {
            buffer[j] = null;
        }
    }
}
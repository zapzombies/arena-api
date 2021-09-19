package io.github.zap.arenaapi.pathfind.step;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.commons.vectors.*;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

class WalkNodeStepper implements NodeStepper {
    private static final Vector3D BLOCK_OFFSET = Vectors.of(0.5, 0, 0.5);

    private Vector3D lastAgentPosition;
    private BoundingBox cachedAgentBounds = null;

    WalkNodeStepper() {}

    @Override
    public @Nullable Vector3I stepDirectional(@NotNull BlockCollisionProvider collisionProvider,
                                              @NotNull BlockCollisionView blockAtFeet, @NotNull PathAgent agent,
                                              @NotNull Vector3D position, @NotNull Direction direction) {
        return switch (direction) {
            case UP, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST ->
                    doStep(collisionProvider, blockAtFeet, agent, position, direction);
            default -> null;
        };
    }

    private Vector3I doStep(BlockCollisionProvider collisionProvider, BlockCollisionView blockAtFeet, PathAgent agent,
                            Vector3D position, Direction direction) {
        Vector3D translate = computeTranslation(position, direction);

        BoundingBox agentBounds = getAgentBounds(agent, position);
        BlockCollisionProvider.HitResult jumpTestResult = collisionProvider
                .collisionMovingAlong(agentBounds, direction, translate);

        if(direction == Direction.UP && !jumpTestResult.blockAtAgent()) {
            return null;
        }

        BoundingBox agentBoundsAtTargetNode = agentBounds.clone().shift(translate.x(), translate.y(), translate.z());
        if(jumpTestResult.collides()) { //test if we need to jump
            if(direction.isIntercardinal() || direction == Direction.UP) { //mobs can't jump diagonally (thanks mojang)
                return null;
            }

            Vector3D seek = seekDirectional(collisionProvider, agent, agentBoundsAtTargetNode.clone(), true);
            Vector3D shift = jumpTestResult.translationVector();

            if(seek != null) {
                BoundingBox adjusted = agentBounds.clone().shift(shift.x(), shift.y(), shift.z());
                double dY = seek.y() - agentBounds.getMinY();

                if(!collisionProvider.collisionMovingAlong(adjusted, Direction.UP, Vectors.of(0, dY, 0)).collides()
                        && !collisionProvider.collisionMovingAlong(adjusted.shift(0, dY, 0), direction,
                        Vectors.subtract(translate, shift)).collides()) {
                    return Vectors.asIntFloor(seek);
                }
            }
        }
        else {
            if(direction == Direction.UP) {
                if(blockAtFeet != null) {
                    if(blockAtFeet.collision().isEmpty()) {
                        return null;
                    }

                    double exactY = blockAtFeet.exactY();
                    if(exactY > position.y() && (exactY - position.y()) < agent.jumpHeight()) {
                        return Vectors.asIntFloor(position.x(), exactY, position.z());
                    }
                }

                return null;
            }

            Vector3D result = seekDirectional(collisionProvider, agent, agentBoundsAtTargetNode, false);

            if(result != null && !Vectors.fuzzyEquals(result, position)) {
                return Vectors.asIntFloor(result);
            }
        }

        return null;
    }

    //WARNING mutates shiftedBounds, currently is never called multiple times on the same execution path
    private Vector3D seekDirectional(BlockCollisionProvider collisionProvider, PathAgent agent, BoundingBox shiftedBounds,
                                     boolean isJump) {
        double maximumDelta = isJump ? agent.jumpHeight() : agent.fallTolerance();
        double delta = 0;

        do {
            List<BlockCollisionView> collisions = collisionProvider.solidsOverlapping(shiftedBounds);

            double stepDelta;
            if(collisions.isEmpty()) {
                if(isJump) { //termination condition for jumping
                    return Vectors.of(shiftedBounds.getCenterX(), shiftedBounds.getMinY(), shiftedBounds.getCenterZ());
                }
                else {
                    stepDelta = -agent.height();
                }
            }
            else {
                BlockCollisionView highest = selectHighest(collisions);

                if(isJump) {
                    stepDelta = highest.exactY() - shiftedBounds.getMinY();
                }
                else { //termination condition for falling
                    return Vectors.of(shiftedBounds.getCenterX(), highest.exactY(), shiftedBounds.getCenterZ());
                }
            }

            shiftedBounds.shift(0, stepDelta, 0);
            delta += Math.abs(stepDelta); //stepDelta i'm stuck
        }
        while(DoubleMath.fuzzyCompare(delta, maximumDelta, Vectors.EPSILON) <= 0);

        return null;
    }

    private BoundingBox getAgentBounds(PathAgent agent, Vector3D newAgentPosition) {
        if(lastAgentPosition == null || !Vectors.equals(newAgentPosition, lastAgentPosition)) {
            double halfWidth = agent.width() / 2;
            double height = agent.height();
            cachedAgentBounds = new BoundingBox(
                    newAgentPosition.x() - halfWidth,
                    newAgentPosition.y(),
                    newAgentPosition.z() - halfWidth,
                    newAgentPosition.x() + halfWidth,
                    newAgentPosition.y() + height,
                    newAgentPosition.z() + halfWidth);

            lastAgentPosition = newAgentPosition;
        }

        return cachedAgentBounds;
    }

    private BlockCollisionView selectHighest(Iterable<BlockCollisionView> collisions) {
        double largestY = Double.MIN_VALUE;
        BlockCollisionView highestBlock = null;

        for(BlockCollisionView collisionView : collisions) {
            double maxY = collisionView.exactY();
            if(maxY > largestY) {
                largestY = maxY;
                highestBlock = collisionView;
            }
        }

        return highestBlock;
    }

    private Vector3D computeTranslation(Vector3D agentPosition, Direction direction) {
        Vector3I agentBlockPosition = Vectors.asIntFloor(agentPosition);
        Vector3I targetBlock = Vectors.add(agentBlockPosition, direction);
        Vector3D targetBlockCenter = Vectors.add(targetBlock, BLOCK_OFFSET);
        return Vectors.of(targetBlockCenter.x() - agentPosition.x(), direction.y(),
                targetBlockCenter.z() - agentPosition.z());
    }
}
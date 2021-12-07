package io.github.zap.arenaapi.pathfind.step;

import com.google.common.math.DoubleMath;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.pathfind.agent.PathAgent;
import io.github.zap.arenaapi.pathfind.collision.BlockCollisionProvider;
import io.github.zap.commons.vectors.*;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
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
                                              @NotNull PathAgent agent,
                                              @NotNull Vector3D position, @NotNull Direction direction,
                                              boolean isFirst) {
        return switch (direction) {
            case UP, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST ->
                    doStep(collisionProvider, agent, position, direction, isFirst);
            default -> null;
        };
    }

    private Vector3I doStep(BlockCollisionProvider collisionProvider, PathAgent agent, Vector3D position,
                            Direction direction, boolean isFirst) {
        Vector3D translate = computeTranslation(position, direction);

        BoundingBox agentBounds = getAgentBounds(agent, position);
        BlockCollisionProvider.HitResult jumpTestResult = collisionProvider.collisionMovingAlong(agentBounds, translate,
                false);

        if(!isFirst) { //if not first
            if(jumpTestResult.blockAtAgent()) { //...and there's a block, return null
                return null;
            }
        }
        else if(direction == Direction.UP && !jumpTestResult.blockAtAgent()) {
            return null;
        }

        if(jumpTestResult.collides()) { //test if we need to jump
            if(direction.isIntercardinal() || direction == Direction.UP) { //mobs can't jump diagonally (thanks mojang)
                return null;
            }

            BlockCollisionView hitBlock = jumpTestResult.nearest();
            BoundingBox agentBoundsAtHit = agentBounds.clone().shift(new Vector(hitBlock.x() + 0.5, position.y(),
                    hitBlock.z() + 0.5).subtract(Vectors.asBukkit(position)));
            Vector3D seek = seekDirectional(collisionProvider, agent, agentBoundsAtHit, hitBlock.x(), position.y(),
                    hitBlock.z(), true);

            if(seek != null) {
                Vector3D shift = jumpTestResult.translationVector();
                BoundingBox adjusted = agentBounds.clone().shift(shift.x(), shift.y(), shift.z());
                double dY = seek.y() - position.y();

                boolean shiftCollides = collisionProvider.collisionMovingAlong(adjusted, Vectors.of(0, dY, 0),
                        true).collides();
                if(!shiftCollides) {
                    BoundingBox adjustedJumped = adjusted.shift(0, dY,0);

                    boolean shiftCollidesJump = collisionProvider.collisionMovingAlong(adjustedJumped,
                            Vectors.of(seek.x() - adjustedJumped.getCenterX(), 0, seek.z() -
                                    adjustedJumped.getCenterZ()), true).collides();

                    if(!shiftCollidesJump) {
                        return Vectors.asIntFloor(seek);
                    }
                }
            }
        }
        else {
            BoundingBox agentBoundsAtTarget = agentBounds.clone().shift(translate.x(), translate.y(), translate.z());
            Vector3D target = Vectors.add(position, translate);
            Vector3D seek = seekDirectional(collisionProvider, agent, agentBoundsAtTarget, target.x(), target.y(),
                    target.z(), false);
            if(seek != null) {
                return Vectors.asIntFloor(seek);
            }
        }

        return null;
    }

    //WARNING mutates shiftedBounds, currently is never called multiple times on the same execution path
    private Vector3D seekDirectional(BlockCollisionProvider collisionProvider, PathAgent agent,
                                     BoundingBox shiftedBounds, double sX, double sY, double sZ, boolean isJump) {
        double maximumDelta = isJump ? agent.jumpHeight() : agent.fallTolerance();
        double delta = 0;

        do {
            List<BlockCollisionView> collisions = collisionProvider.solidsOverlapping(shiftedBounds);

            double stepDelta; //stepDelta i'm stuck
            if(collisions.isEmpty()) {
                if(isJump) { //termination condition for jumping
                    return Vectors.of(sX + 0.5, sY, sZ + 0.5);
                }
                else {
                    stepDelta = -agent.height();
                }
            }
            else {
                BlockCollisionView highest = selectHighest(collisions);

                sX = highest.x();
                sY = highest.exactY();
                sZ = highest.z();

                if(isJump) {
                    stepDelta = highest.exactY() - shiftedBounds.getMinY();
                }
                else { //termination condition for falling
                    return Vectors.of(sX + 0.5, sY, sZ + 0.5);
                }
            }

            shiftedBounds.shift(0, stepDelta, 0);
            delta += Math.abs(stepDelta);
        }
        while(DoubleMath.fuzzyCompare(delta, maximumDelta, Vectors.EPSILON) <= 0);

        return null;
    }

    private BoundingBox getAgentBounds(PathAgent agent, Vector3D position) {
        if(lastAgentPosition == null || !Vectors.fuzzyEquals(position, lastAgentPosition)) {
            double halfWidth = agent.width() / 2;
            double height = agent.height();
            cachedAgentBounds = new BoundingBox(
                    position.x() - halfWidth, position.y(),
                    position.z() - halfWidth, position.x() + halfWidth,
                    position.y() + height, position.z() + halfWidth);

            lastAgentPosition = position;
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
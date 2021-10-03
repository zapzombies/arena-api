package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import io.github.zap.arenaapi.nms.common.util.BoundedBlockIterator;
import io.github.zap.commons.vectors.*;
import io.github.zap.commons.vectors.Vector2I;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.World;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    protected final World world;
    protected final Map<Long, CollisionChunkView> chunkViewMap;

    private final boolean supportsAsync;

    BlockCollisionProviderAbstract(@NotNull World world, @NotNull Map<Long, CollisionChunkView> chunkViewMap,
                                   boolean supportsAsync) {
        this.world = world;
        this.chunkViewMap = chunkViewMap;
        this.supportsAsync = supportsAsync;
    }

    @Override
    public @NotNull World world() {
        return world;
    }

    @Override
    public boolean supportsAsync() {
        return supportsAsync;
    }

    @Override
    public void updateRegion(@NotNull ChunkBounds coordinates) {}

    @Override
    public void clearRegion(@NotNull ChunkBounds coordinates) {}

    @Override
    public void clearForWorld() {}

    @Override
    public @Nullable BlockCollisionView getBlock(int x, int y, int z) {
        CollisionChunkView view = chunkAt(x >> 4, z >> 4);

        if(view != null) {
            return view.getBlock(x & 15, y, z & 15);
        }

        return null;
    }

    @Override
    public boolean collidesAt(@NotNull BoundingBox worldRelativeBounds) {
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            Vector2I chunkCoordinates = iterator.next();
            CollisionChunkView chunk = chunkAt(chunkCoordinates.x(), chunkCoordinates.z());

            if(chunk != null && chunk.collidesWithAny(worldRelativeBounds)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull List<BlockCollisionView> solidsOverlapping(@NotNull BoundingBox worldRelativeBounds) {
        List<BlockCollisionView> shapes = new ArrayList<>();
        ChunkBoundsIterator iterator = new ChunkBoundsIterator(worldRelativeBounds);

        while(iterator.hasNext()) {
            Vector2I chunkCoordinates = iterator.next();
            CollisionChunkView chunk = chunkAt(chunkCoordinates.x(), chunkCoordinates.z());

            if(chunk != null) {
                shapes.addAll(chunk.collisionsWith(worldRelativeBounds));
            }
        }

        return shapes;
    }

    @Override
    public @NotNull HitResult collisionMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Vector3D translation,
                                                   boolean fastExit) {
        BoundingBox expanded = agentBounds.clone().expandDirectional(translation.x(), translation.y(), translation.z());

        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(this, expanded);
        return collisionCheck(agentBounds, expanded, translation.x(), translation.y(), translation.z(), iterator,
                fastExit);
    }

    protected long chunkKey(int x, int z) {
        //from https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
        return (((long)x) << 32) | (z & 0xFFFFFFFFL);
    }

    /*
    checks for collisions the provided boundingbox has while moving along the specified vector, checking only those
    BlockCollisionView objects that appear in the candidates list. views intersecting the original position of the
    entity will be discarded.
     */
    private HitResult collisionCheck(BoundingBox agentBounds, BoundingBox expanded, double tX, double tY, double tZ,
                                     Iterator<BlockCollisionView> candidates, boolean fastExit) {
        double width = agentBounds.getWidthX();
        double height = agentBounds.getHeight();

        double originX = agentBounds.getCenterX();
        double originY = agentBounds.getCenterY();
        double originZ = agentBounds.getCenterZ();

        double adjustedXZ = (width * (Math.abs(tX) + Math.abs(tZ))) / 2;
        double adjustedXY = (height * (Math.abs(tX) + Math.abs(tY))) / 2;
        double adjustedZY = (height * (Math.abs(tZ) + Math.abs(tY))) / 2;

        double halfWidth = width / 2;
        double halfHeight = height / 2;

        double nearestLengthSquared = Double.MAX_VALUE;
        boolean foundCollision = false;
        boolean collidesAtAgent = false;
        BlockCollisionView nearestBlock = null;
        Vector offset = new Vector();

        while(candidates.hasNext()) {
            BlockCollisionView view = candidates.next();

            if(view != null && !view.collision().isEmpty()) {
                if(view.overlaps(agentBounds)) {
                    collidesAtAgent = true;
                }
                else if(view.overlaps(expanded)) {
                    for(Bounds shapeBounds : view.collision()) {
                        double minX = ((shapeBounds.minX() + view.x()) - originX);
                        double minY = ((shapeBounds.minY() + view.y()) - originY);
                        double minZ = ((shapeBounds.minZ() + view.z()) - originZ);

                        double maxX = ((shapeBounds.maxX() + view.x()) - originX);
                        double maxY = ((shapeBounds.maxY() + view.y()) - originY);
                        double maxZ = ((shapeBounds.maxZ() + view.z()) - originZ);

                        if(checkPair(adjustedXZ, tX, tZ, minX, minZ, maxX, maxZ) &&
                                checkPair(adjustedXY, tX, tY, minX, minY, maxX, maxY) &&
                                checkPair(adjustedZY, tZ, tY, minZ, minY, maxZ, maxY)) {
                            if(fastExit) { //don't compute translation vector if we're fast-exit
                                return new HitResult(true, collidesAtAgent, null, null);
                            }
                            else { //not fast exit, need to run additional checks
                                double thisDistance;
                                if((thisDistance = processCollision(halfWidth, halfHeight, tX, tY, tZ,
                                        minX, minY, minZ, maxX, maxY, maxZ, offset, nearestLengthSquared))!= -1) {
                                    nearestLengthSquared = thisDistance;
                                    nearestBlock = view;
                                    foundCollision = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return new HitResult(foundCollision, collidesAtAgent, nearestBlock, Vectors.of(offset));
    }

    /*
    checks for collisions when travelling along a certain plane. as checkPlane expects the min and max vectors to be
    ordered in a certain way, performs a simple check on the 'slope' of the inequalities used for representing the
    moving bounding box (see checkPlane)
     */
    private boolean checkPair(double adjustedSize, double dirA, double dirB, double minA, double minB,
                              double maxA, double maxB) {
        if(dirA == 0 && dirB == 0) { //degenerate case; assume true
            return true;
        }

        if(dirA * dirB <= 0) {
            return checkPlane(adjustedSize, dirA, dirB, minA, minB, maxA, maxB);
        }
        else {
            return checkPlane(adjustedSize, dirA, dirB, maxA, minB, minA, maxB);
        }
    }

    /*
    computes the translation vector from a bounding box with width halfWidth * 2, height halfHeight * 2 along the vector
    represented by <dirX, dirY, dirZ> to a (entity origin relative) bounds represented by the two points
    (minX, minY, minZ) and (maxX, maxY, maxZ). if the distance (magnitude of the translation vector) is smaller than
    nearestLengthSquared, vector nearest will have no data written to it, and the function will return -1. otherwise,
    nearest will contain the new translation vector, and its magnitude will be returned (which must be less than
    nearestLengthSquared
     */
    private double processCollision(double halfWidth, double halfHeight, double dirX, double dirY, double dirZ,
                                    double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                    Vector nearest, double nearestLengthSquared) {
        double signX = Math.signum(dirX);
        double signY = Math.signum(dirY);
        double signZ = Math.signum(dirZ);

        double agentX = halfWidth * signX;
        double agentY = halfHeight * signY;
        double agentZ = halfWidth * signZ;

        double deltaMinX = Math.abs(agentX - minX);
        double deltaMaxX = Math.abs(agentX - maxX);

        double deltaMinY = Math.abs(agentY - minY);
        double deltaMaxY = Math.abs(agentY - maxY);

        double deltaMinZ = Math.abs(agentZ - minZ);
        double deltaMaxZ = Math.abs(agentZ - maxZ);

        double x = Math.min(deltaMinX, deltaMaxX);
        double y = Math.min(deltaMinY, deltaMaxY);
        double z = Math.min(deltaMinZ, deltaMaxZ);

        double thisLengthSquared = x * x + y * y + z * z;
        if(thisLengthSquared < nearestLengthSquared) {
            nearest.setX(x * signX);
            nearest.setY(y * signY);
            nearest.setZ(z * signZ);
            return thisLengthSquared;
        }

        return -1;
    }

    /*
    this simple algorithm determines if a given bounds, denoted by a pair of 2d points, intersects the path traced by
    a bounding box moving in the direction denoted by the vector <dirX, dirZ>. the width of the bounding box is given
    by adjustedWidth, whose value must be precalculated as follows:

    (width * (Math.abs(dirX) + Math.abs(dirZ))) / 2

    the function works by testing points (min, max) against a pair of inequalities:

    First: (z * dirX) - (x * dirZ) < w
    Second: (z * dirX) - (x * dirZ) > -w

    the function follows the truth table shown below. a question mark denotes "don't cares"

    minInFirst   |   minInSecond   |   maxInFirst   |   maxInSecond   |   collides
    0                1                 0                ?                 0
    0                1                 1                ?                 1
    1                0                 ?                0                 0
    1                0                 ?                1                 1  //same as #2 but inverted
    1                1                 ?                ?                 1

    some combinations of values are not possible given valid inputs, and thus they are not present in the truth table
    and are not tested for either. for example, a point that satisfies neither of the inequalities is not possible for
    a valid adjustedWidth parameter.

    more specifically, regarding invalid input, all double parameters must be finite, and adjustedWidth must be greater
    than 0. dirX and dirZ may be any pair of integers, including negative numbers, but they cannot both be zero (one
    may be zero if the other is non-zero). minX, minZ, maxX, and maxZ must be finite and have an additional special
    consideration that a vector drawn between them must NOT belong to the same or opposite quadrant as the direction
    vector
    */
    private boolean checkPlane(double adjustedSize, double dirA, double dirB, double minA, double minB,
                               double maxA, double maxB) {
        double bMinusAMin = (minB * dirA) - (minA * dirB);
        if(bMinusAMin >= adjustedSize) { //!minInFirst
            return (maxB * dirA) - (maxA * dirB) < adjustedSize;  //... && maxInFirst
        }

        //we know minInFirst is true
        if(bMinusAMin > -adjustedSize) { //... && minInSecond
            return true;
        }

        return (maxB * dirA) - (maxA * dirB) > -adjustedSize; // ... && !minInSecond
    }
}
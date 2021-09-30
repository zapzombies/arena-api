package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import io.github.zap.commons.vectors.*;
import io.github.zap.commons.vectors.Vector2I;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vectors;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    private static final Vector ZERO = new Vector();

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
            return view.collisionView(x & 15, y, z & 15);
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
    public @NotNull HitResult collisionMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Vector3D translation) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(
                translation.x(), translation.y(), translation.z()).expand(-Vectors.EPSILON);

        List<BlockCollisionView> samples = solidsOverlapping(expandedBounds);
        return collisionCheck(agentBounds, translation.x(), translation.y(), translation.z(), samples);
    }

    protected long chunkKey(int x, int z) {
        //from https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
        return (((long)x) << 32) | (z & 0xFFFFFFFFL);
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
    private HitResult collisionCheck(BoundingBox agentBounds, double dirX, double dirY, double dirZ,
                                     Iterable<BlockCollisionView> candidates) {
        double width = agentBounds.getWidthX();
        double height = agentBounds.getHeight();

        double originX = agentBounds.getCenterX();
        double originY = agentBounds.getCenterY();
        double originZ = agentBounds.getCenterZ();

        double adjustedWidth = (width * (Math.abs(dirX) + Math.abs(dirZ))) / 2;
        double adjustedHeight = (height * (Math.abs(dirX) + Math.abs(dirY))) / 2;

        double halfWidth = width / 2;
        double halfHeight = height / 2;

        double dfZ = dirX * dirZ;
        double dfY = dirX * dirY;

        double nearestLengthSquared = Double.MAX_VALUE;
        boolean foundCollision = false;
        boolean collidesAtAgent = false;
        BlockCollisionView nearestBlock = null;
        Vector offset = new Vector();

        for(BlockCollisionView view : candidates) {
            if(view.isOverlapping(agentBounds)) {
                collidesAtAgent = true;
                continue;
            }

            for(Bounds shapeBounds : view.collision()) {
                double x, y, z, x2, y2, z2;

                if(dfZ < 0) {
                    x = (shapeBounds.minX() + view.x()) - originX;
                    z = (shapeBounds.minZ() + view.z()) - originZ;

                    x2 = (shapeBounds.maxX() + view.x()) - originX;
                    z2 = (shapeBounds.maxZ() + view.z()) - originZ;
                }
                else {
                    x = (shapeBounds.maxX() + view.x()) - originX;
                    z = (shapeBounds.minZ() + view.z()) - originZ;

                    x2 = (shapeBounds.minX() + view.x()) - originX;
                    z2 = (shapeBounds.maxZ() + view.z()) - originZ;
                }

                if(checkBounds(adjustedWidth, dirX, dirZ, x, z, x2, z2)) {
                    double lengthSquared = -1;

                    if(dirY == 0) { //no need to check y if we are moving horizontally
                        lengthSquared = processCollision(halfWidth, halfHeight, dirX, dirY, dirZ, x, 0, z,
                                x2, 0, z2, offset, nearestLengthSquared);
                    }
                    else { //calculate y
                        if(dfY < 0) {
                            y = (shapeBounds.minY() + view.y()) - originY;
                            y2 = (shapeBounds.maxY() + view.y()) - originY;
                        }
                        else {
                            y = (shapeBounds.minY() + view.y()) - originY;
                            y2 = (shapeBounds.maxY() + view.y()) - originY;
                        }

                        if(checkBounds(adjustedHeight, dirX, dirY, x, y, x2, y2)) {
                            lengthSquared = processCollision(halfWidth, halfHeight, dirX, dirY, dirZ,
                                    x, y, z, x2, y2, z2, offset, nearestLengthSquared);
                        }
                    }

                    if(lengthSquared != -1) {
                        nearestLengthSquared = lengthSquared;
                        nearestBlock = view;
                        foundCollision = true;
                    }
                }
            }
        }

        return new HitResult(foundCollision, collidesAtAgent, nearestBlock, Vectors.of(offset));
    }

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

        double x = Math.min(deltaMinX, deltaMaxX) * signX;
        double y = Math.min(deltaMinY, deltaMaxY) * signY;
        double z = Math.min(deltaMinZ, deltaMaxZ) * signZ;

        double thisLengthSquared = x * x + y * y + z * z;
        if(thisLengthSquared < nearestLengthSquared || nearest.equals(ZERO)) {
            nearest.setX(x);
            nearest.setY(y);
            nearest.setZ(z);
            return thisLengthSquared;
        }

        return -1;
    }

    private boolean checkBounds(double adjustedSize, double dirA, double dirB, double minA, double minB,
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
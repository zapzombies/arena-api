package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.CollisionChunkView;
import io.github.zap.arenaapi.nms.common.world.VoxelShapeWrapper;
import io.github.zap.arenaapi.pathfind.chunk.ChunkBounds;
import io.github.zap.arenaapi.pathfind.util.ChunkBoundsIterator;
import io.github.zap.commons.vectors.*;
import io.github.zap.commons.vectors.Vector2I;
import io.github.zap.commons.vectors.Vector3D;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class BlockCollisionProviderAbstract implements BlockCollisionProvider {
    private record HitTestResult(HitTestType type, Vector3D min, Vector3D max) {}

    @FunctionalInterface
    private interface ViewPredicate {
        @NotNull HitTestResult test(BlockCollisionView block, Vector3D shapeVector);
    }

    private enum HitTestType {
        HITS,
        MIN_NOT_IN_FIRST,
        MIN_IN_FIRST_MAX_IN_SECOND,
        MIN_IN_FIRST_AND_SECOND,
        NO_COLLISION;

        private boolean hits() {
            return this != NO_COLLISION;
        }
    }

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
    public @NotNull HitResult collisionMovingAlong(@NotNull BoundingBox agentBounds, @NotNull Direction direction,
                                                   @NotNull Vector3D translation) {
        BoundingBox expandedBounds = agentBounds.clone().expandDirectional(Vectors.asBukkit(translation)).expand(-Vectors.EPSILON);

        double width = agentBounds.getWidthX();

        List<BlockCollisionView> collisionViews = solidsOverlapping(expandedBounds);
        boolean hitsAtAgent = removeCollidingAtAgent(agentBounds, collisionViews);

        if(direction.isAxisAligned()) {
            if(collisionViews.isEmpty()) { //for N, E, S, W, U and D there is no collision if collisionViews is empty
                return HitResult.NO_HIT;
            }

            Direction opposite = direction.opposite();
            return nearestView(collisionViews, agentBounds, direction, (shape, shapeVector) -> {
                Bounds face = shape.collision().boundingBox().positionDirectional(opposite);

                return new HitTestResult(HitTestType.HITS, Vectors.add(face.min(), shapeVector),
                        Vectors.add(face.max(), shapeVector));
            }, hitsAtAgent);
        }
        else if(direction.isIntercardinal()) {
            Direction first = direction.rotateClockwise();
            Direction second = first.opposite();

            double adjustedWidth = (width * (Math.abs(direction.x()) + Math.abs(direction.z()))) / 2;

            return nearestView(collisionViews, agentBounds, direction, (shape, shapeVector) -> {
                VoxelShapeWrapper collision = shape.collision();

                Bounds firstLine = collision.boundingBox().positionDirectional(first);
                Bounds secondLine = collision.boundingBox().positionDirectional(second);

                Vector3D firstPoint = Vectors.add(firstLine.min(), shapeVector);
                Vector3D secondPoint = Vectors.add(secondLine.min(), shapeVector);

                HitTestType type = collisionCheck(adjustedWidth, direction.x(), direction.z(),
                        firstPoint.x(), firstPoint.z(),
                        secondPoint.x(), secondPoint.z());

                return new HitTestResult(type, firstPoint, secondPoint);
            }, hitsAtAgent);
        }
        else {
            throw new IllegalArgumentException("Direction " + direction + " not supported");
        }
    }

    protected long chunkKey(int x, int z) {
        //from https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
        return (((long)x) << 32) | (z & 0xFFFFFFFFL);
    }

    private HitResult nearestView(Iterable<BlockCollisionView> collisions, BoundingBox agentBounds,
                                  Direction direction, ViewPredicate filter, boolean collisionAtEntity) {
        double halfWidth = agentBounds.getWidthX() / 2;
        double nearestMagnitudeSquared = Double.POSITIVE_INFINITY;
        Vector3D nearestTranslation = null;
        BlockCollisionView nearestCollision = null;
        boolean collides = false;

        Vector3D agentCorner = Vectors.subtract(Bounds.positionDirectional(direction, agentBounds.getMinX(),
                agentBounds.getMinY(), agentBounds.getMinZ(), agentBounds.getMaxX(), agentBounds.getMaxY(),
                agentBounds.getMaxZ()).min(), Vectors.of(halfWidth + agentBounds.getMinX(), 0, halfWidth + agentBounds.getMinZ()));

        for(BlockCollisionView shape : collisions) {
            Vector3D shapeVector = Vectors.of(shape.x() - agentBounds.getCenterX(),
                    shape.y() - agentBounds.getMinY(), shape.z() - agentBounds.getCenterZ());

            HitTestResult hitTestResult;
            if((hitTestResult = filter.test(shape, shapeVector)).type().hits()) {
                double deltaMinX = Math.abs(agentCorner.x() - hitTestResult.min.x());
                double deltaMaxX = Math.abs(agentCorner.x() - hitTestResult.max.x());

                double deltaMinY = Math.abs(agentCorner.y() - hitTestResult.min.y());
                double deltaMaxY = Math.abs(agentCorner.y() - hitTestResult.max.y());

                double deltaMinZ = Math.abs(agentCorner.z() - hitTestResult.min.z());
                double deltaMaxZ = Math.abs(agentCorner.z() - hitTestResult.max.z());

                double xComp = Math.min(deltaMinX, deltaMaxX) * direction.x();
                double yComp = Math.min(deltaMinY, deltaMaxY) * direction.y();
                double zComp = Math.min(deltaMinZ, deltaMaxZ) * direction.z();

                double currentMagnitudeSquared = Vectors.distanceSquared(xComp, yComp, zComp, 0, 0, 0);

                if(currentMagnitudeSquared < nearestMagnitudeSquared) {
                    nearestCollision = shape;
                    nearestTranslation = Vectors.of(xComp, yComp, zComp);
                    nearestMagnitudeSquared = currentMagnitudeSquared;
                    collides = true;
                }
            }
        }

        return new HitResult(collides, collisionAtEntity, nearestCollision, nearestTranslation);
    }

    private boolean removeCollidingAtAgent(BoundingBox agentBounds, List<BlockCollisionView> hits) {
        boolean foundOne = false;
        for(int i = hits.size() - 1; i >= 0; i--) {
            if(hits.get(i).overlaps(agentBounds)) {
                hits.remove(i);
                foundOne = true;
            }
        }

        return foundOne;
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
    private HitTestType collisionCheck(double adjustedWidth, int dirX, int dirZ, double minX, double minZ,
                                       double maxX, double maxZ) {
        double zMinusXMin = (minZ * dirX) - (minX * dirZ);
        if(zMinusXMin >= adjustedWidth) { //!minInFirst
            return (maxZ * dirX) - (maxX * dirZ) < adjustedWidth ? HitTestType.MIN_NOT_IN_FIRST :
                    HitTestType.NO_COLLISION;
        }

        //we know minInFirst is true...

        if(zMinusXMin > -adjustedWidth) { //minInFirst && minInSecond
            return HitTestType.MIN_IN_FIRST_AND_SECOND;
        }

        return (maxZ * dirX) - (maxX * dirZ) > -adjustedWidth ? HitTestType.MIN_IN_FIRST_MAX_IN_SECOND :
                HitTestType.NO_COLLISION; // minInFirst && !minInSecond
    }
}
package io.github.zap.arenaapi.pathfind.collision;

import io.github.zap.arenaapi.nms.common.world.*;
import io.github.zap.commons.vectors.*;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ProxyBlockCollisionProviderTest {
    private WorldBridge worldBridge;
    private World world;
    private ProxyBlockCollisionProvider provider;

    private final Map<Vector2I, CollisionChunkView> mockChunkViews = new HashMap<>();

    private final BoundingBox fullAgentBounds = new BoundingBox(0, 0, 0, 1, 2, 1);
    private final BoundingBox tinyAgentBounds = new BoundingBox(0.4, 0, 0.4, 0.6, 2, 0.6);
    private final BoundingBox tallAgentBounds = new BoundingBox(0.2, 0, 0.2, 0.8, 3, 0.8);
    private final BoundingBox thickAgentBounds = new BoundingBox(-0.5, 0, -0.5, 1.5, 2, 1.5);

    private final List<BoundingBox> fullBlock = new ArrayList<>();
    private final List<BoundingBox> tinyBlock = new ArrayList<>();
    private final List<BoundingBox> stairBlock = new ArrayList<>();
    private final List<BoundingBox> bottomSlab = new ArrayList<>();

    private final Bounds fullBlockBounds = new Bounds(0, 0, 0, 1, 1, 1);
    private final Bounds tinyBlockBounds = new Bounds(0.4, 0, 0.4, 0.6, 1, 0.6);
    private final Bounds lowerHalfBlockBounds = new Bounds(0, 0, 0, 1, 0.5, 1);
    private final Bounds upperHalfBlockBounds = new Bounds(0, 0.5, 0, 1, 1, 1);

    @BeforeEach
    void setUp() {
        worldBridge = Mockito.mock(WorldBridge.class);
        world = Mockito.mock(World.class);
        provider = new ProxyBlockCollisionProvider(worldBridge, world, 1);

        fullBlock.add(new BoundingBox(0, 0, 0, 1, 1, 1));
        tinyBlock.add(new BoundingBox(0.4, 0, 0.4, 0.6, 1, 0.6));

        stairBlock.add(new BoundingBox(0, 0.5, 0, 1, 1, 1));
        stairBlock.add(new BoundingBox(0, 0, 0, 0.5, 0.5, 1));

        bottomSlab.add(new BoundingBox(0, 0, 0, 1, 0.5, 1));
    }

    @Test
    void collidesMovingAlongNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.collisionMovingAlong(bounds,
                Vectors.asDouble(Direction.NORTH)));
    }

    @Test
    void collidesAtNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.collidesAt(bounds));
    }

    @Test
    void collidingSolidsAtNoModification() {
        assertNoModification(fullAgentBounds, (bounds) -> provider.solidsOverlapping(bounds));
    }

    @Test
    void fullAgentMovingCardinalCollisionWithFullCardinalBlocks() {
        testCardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock, fullBlockBounds), true,
                Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE);
    }

    @Test
    void fullAgentMovingIntercardinalCollisionWithFullCardinalBlocks() {
        testIntercardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock, fullBlockBounds), true,
                Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE, Vectors.ZERO_DOUBLE);
    }

    @Test
    void fullAgentMovingCardinalCollisionWithTinyCardinalBlocks() {
        testCardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock, tinyBlockBounds), true,
                Vectors.of(0, 0, -0.4), Vectors.of(0.4, 0, 0),
                Vectors.of(0, 0, 0.4), Vectors.of(-0.4, 0, 0));
    }

    @Test
    void fullAgentMovingIntercardinalCollisionWithTinyCardinalBlocks() {
        testIntercardinal(fullAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock, tinyBlockBounds), true,
                Vectors.of(0.4, 0, -0.4), Vectors.of(0.4, 0, 0.4),
                Vectors.of(-0.4, 0, 0.4), Vectors.of(-0.4, 0, -0.4));
    }

    @Test
    void fullAgentMovingCardinalNoCollision() {
        testCardinalSameCollision(fullAgentBounds, new ArrayList<>(), false);
    }

    @Test
    void fullAgentMovingIntercardinalNoCollision() {
        testIntercardinalSameCollision(fullAgentBounds, new ArrayList<>(), false);
    }

    @Test
    void tinyAgentMovingCardinalCollisionWithFullCardinalBlocks() {
        testCardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock, fullBlockBounds), true,
                Vectors.of(0, 0, -0.4), Vectors.of(0.4, 0, 0),
                Vectors.of(0, 0, 0.4), Vectors.of(-0.4, 0, 0));
    }

    @Test
    void tinyAgentMovingIntercardinalCollisionWithFullCardinalBlocks() {
        testIntercardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(fullBlock, fullBlockBounds), true,
                Vectors.of(0.4, 0, -0.4), Vectors.of(0.4, 0, 0.4),
                Vectors.of(-0.4, 0, 0.4), Vectors.of(-0.4, 0, -0.4));
    }

    @Test
    void tinyAgentMovingCardinalCollisionWithTinyCardinalBlocks() {
        testCardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock, tinyBlockBounds), true,
                Vectors.of(0, 0, -0.8), Vectors.of(0.8, 0, 0),
                Vectors.of(0, 0, 0.8), Vectors.of(-0.8, 0, 0));
    }

    @Test
    void tinyAgentMovingIntercardinalNoCollisionWithTinyCardinalBlocks() {
        testIntercardinal(tinyAgentBounds, createCardinallyAdjacentTestBlocks(tinyBlock, tinyBlockBounds), false);
    }

    @Test
    void fullAgentMovingCardinalNoCollisionFullBlockOverlappingFeet() {
        BlockCollisionView block = mockBlockAt(0, 0, 0, fullBlock, fullBlockBounds, true);
        testCardinalSameCollision(fullAgentBounds, List.of(block), false);
    }

    @Test
    void fullAgentMovingIntercardinalNoCollisionFullBlockOverlappingFeet() {
        BlockCollisionView block = mockBlockAt(0, 0, 0, fullBlock, fullBlockBounds,true);
        testIntercardinalSameCollision(fullAgentBounds, List.of(block), false);
    }

    @Test
    void fullAgentMovingCardinalNoCollisionFullBlockOverlappingFeetAndHead() {
        BlockCollisionView blockWaist = mockBlockAt(0, 0, 0, fullBlock, fullBlockBounds, true);
        BlockCollisionView blockHead = mockBlockAt(0, 1, 0, fullBlock, fullBlockBounds, true);
        testCardinalSameCollision(fullAgentBounds, List.of(blockWaist, blockHead), false);
    }

    @Test
    void fullAgentMovingIntercardinalNoCollisionFullBlockOverlappingFeetAndHead() {
        BlockCollisionView blockWaist = mockBlockAt(0, 0, 0, fullBlock, fullBlockBounds,true);
        BlockCollisionView blockHead = mockBlockAt(0, 1, 0, fullBlock, fullBlockBounds,true);
        testIntercardinalSameCollision(fullAgentBounds, List.of(blockWaist, blockHead), false);
    }

    @Test
    void tallAgentMovingCardinalNoCollisionStairsOverlappingHead() {
        BlockCollisionView blockHead = mockBlockAt(0, 2, 0, stairBlock, fullBlockBounds,true);
        testCardinalSameCollision(tallAgentBounds, List.of(blockHead), false);
    }

    @Test
    void tallAgentMovingCardinalNoCollisionStairsOverlappingFeetAndHead() {
        BlockCollisionView blockHead = mockBlockAt(0, 2, 0, stairBlock, fullBlockBounds,true);
        BlockCollisionView blockFeet = mockBlockAt(0, 0, 0, stairBlock, fullBlockBounds,true);
        testCardinalSameCollision(tallAgentBounds, List.of(blockHead, blockFeet), false);
    }

    @Test
    void fullAgentMovingCardinalNoCollisionFullBlockAboveHead() {
        mockBlockAt(0, 2, 0, fullBlock, fullBlockBounds, false);
        testCardinalSameCollision(fullAgentBounds, new ArrayList<>(), false);
    }

    @Test
    void fullAgentMovingDownNoCollision() {
        testWalkDirection(fullAgentBounds, new ArrayList<>(), Direction.DOWN, false, null);
    }

    @Test
    void fullAgentMovingDownCollisionWithFullBlock() {
        mockBlockAt(0, 0, 0, fullBlock, fullBlockBounds, false);
        testWalkDirection(fullAgentBounds.clone().shift(0, 1, 0), new ArrayList<>(), Direction.DOWN,
                false, null);
    }

    @Test
    void fullAgentMovingDownCollisionWithSlab() {
        BlockCollisionView blockFeet = mockBlockAt(0, 0, 0, bottomSlab, lowerHalfBlockBounds, false);
        testWalkDirection(fullAgentBounds.clone().shift(0, 1, 0), List.of(blockFeet), Direction.DOWN,
                true, null);
    }

    @Test
    void fullAgentMovingCardinallyOnSlab() {
        BlockCollisionView blockFeet = mockBlockAt(0, 0, 0, bottomSlab, lowerHalfBlockBounds, false);
        testCardinalSameCollision(fullAgentBounds.clone().shift(0, 0.5, 0), List.of(blockFeet), false);
    }

    private void assertNoModification(BoundingBox bounds, Consumer<BoundingBox> consumer) {
        BoundingBox reference = bounds.clone();
        consumer.accept(bounds);
        Assertions.assertEquals(reference, bounds);
    }

    private CollisionChunkView mockChunkAt(int x, int z) {
        Vector2I location = Vectors.of(x, z);
        if(!mockChunkViews.containsKey(location)) {
            Chunk mockChunk = Mockito.mock(Chunk.class);
            CollisionChunkView mockChunkView = Mockito.mock(CollisionChunkView.class);
            mockChunkViews.put(location, mockChunkView);

            Mockito.when(worldBridge.getChunkIfLoadedImmediately(world, x, z)).thenReturn(mockChunk);
            Mockito.when(worldBridge.proxyView(Mockito.same(mockChunk))).thenReturn(mockChunkView);

            Mockito.when(mockChunkView.position()).thenReturn(Vectors.of(x, z));
            return mockChunkView;
        }

        return mockChunkViews.get(location);
    }

    private BlockCollisionView mockBlockAt(int x, int y, int z, List<BoundingBox> voxelShapes, Bounds blockBounds,
                                           boolean overlapsAtAgent) {
        CollisionChunkView mockChunkView = mockChunkAt(x >> 4, z >> 4);

        VoxelShapeWrapper mockVoxelShapeWrapper = Mockito.mock(VoxelShapeWrapper.class);
        Mockito.when(mockVoxelShapeWrapper.boundingBox()).thenReturn(blockBounds);

        Mockito.when(mockVoxelShapeWrapper.iterator()).thenAnswer(invocation -> voxelShapes.stream().map(bb ->
                new Bounds(bb.getMinX(), bb.getMinY(), bb.getMinZ(), bb.getMaxX(),
                bb.getMaxY(), bb.getMaxZ())).collect(Collectors.toList()).iterator());

        BlockCollisionView mockBlockView = Mockito.mock(BlockCollisionView.class);
        Mockito.when(mockBlockView.collision()).thenReturn(mockVoxelShapeWrapper);
        Mockito.when(mockBlockView.isOverlapping(ArgumentMatchers.any())).thenReturn(overlapsAtAgent);

        Mockito.when(mockBlockView.x()).thenReturn(x);
        Mockito.when(mockBlockView.y()).thenReturn(y);
        Mockito.when(mockBlockView.z()).thenReturn(z);

        Mockito.when(mockChunkView.collisionView(x & 15, y, z & 15)).thenReturn(mockBlockView);
        return mockBlockView;
    }

    private void testWalkDirection(BoundingBox agentBounds, List<BlockCollisionView> initialCollisions,
                                   Direction direction, boolean collides, Vector3D expectedTranslation) {
        int x = NumberConversions.floor(agentBounds.getCenterX());
        int z = NumberConversions.floor(agentBounds.getCenterZ());

        CollisionChunkView chunk = mockChunkAt(x >> 4, z >> 4);
        Mockito.when(chunk.collisionsWith(ArgumentMatchers.any())).thenReturn(initialCollisions);

        BlockCollisionProvider.HitResult result = provider.collisionMovingAlong(agentBounds.clone(),
                Vectors.asDouble(direction));
        Assertions.assertSame(collides, result.collides(), "expected collision to be " + collides + " for " +
                "direction " + direction + " with expected translation " + expectedTranslation);

        if(expectedTranslation != null) {
            Assertions.assertTrue(Vectors.fuzzyEquals(expectedTranslation, result.translationVector()),
                    result.translationVector() + ", should have been " + expectedTranslation + " for direction " + direction);
        }
    }

    private void testCardinal(BoundingBox agentBounds, BlockCollisionView[] samples, boolean collides,
                              Vector3D... expectedTranslations) {
        testWalkDirection(agentBounds, List.of(samples[0]), Direction.NORTH, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[0]);
        testWalkDirection(agentBounds, List.of(samples[1]), Direction.EAST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[1]);
        testWalkDirection(agentBounds, List.of(samples[2]), Direction.SOUTH, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[2]);
        testWalkDirection(agentBounds, List.of(samples[3]), Direction.WEST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[3]);
    }

    private void testIntercardinal(BoundingBox agentBounds, BlockCollisionView[] samples, boolean collides,
                                   Vector3D... expectedTranslations) {
        testWalkDirection(agentBounds, List.of(samples[0], samples[1]), Direction.NORTHEAST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[0]);
        testWalkDirection(agentBounds, List.of(samples[1], samples[2]), Direction.SOUTHEAST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[1]);
        testWalkDirection(agentBounds, List.of(samples[2], samples[3]), Direction.SOUTHWEST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[2]);
        testWalkDirection(agentBounds, List.of(samples[3], samples[0]), Direction.NORTHWEST, collides,
                expectedTranslations.length == 0 ? null : expectedTranslations[3]);
    }

    private void testCardinalSameCollision(BoundingBox agentBounds, List<BlockCollisionView> collisions, boolean collides) {
        testWalkDirection(agentBounds, collisions, Direction.NORTH, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.EAST, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.SOUTH, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.WEST, collides, null);
    }

    private void testIntercardinalSameCollision(BoundingBox agentBounds, List<BlockCollisionView> collisions, boolean collides) {
        testWalkDirection(agentBounds, collisions, Direction.NORTHEAST, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.SOUTHEAST, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.SOUTHWEST, collides, null);
        testWalkDirection(agentBounds, collisions, Direction.NORTHWEST, collides, null);
    }

    private BlockCollisionView[] createCardinallyAdjacentTestBlocks(List<BoundingBox> blockBounds, Bounds blockBoundingBox) {
        BlockCollisionView[] blocks = new BlockCollisionView[4];
        blocks[0] = mockBlockAt(0, 0, -1, blockBounds, blockBoundingBox, false);
        blocks[1] = mockBlockAt(1, 0, 0, blockBounds, blockBoundingBox, false);
        blocks[2] = mockBlockAt(0, 0, 1, blockBounds, blockBoundingBox, false);
        blocks[3] = mockBlockAt(-1, 0, 0, blockBounds, blockBoundingBox, false);
        return blocks;
    }
}
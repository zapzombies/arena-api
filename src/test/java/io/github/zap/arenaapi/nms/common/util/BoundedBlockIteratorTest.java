package io.github.zap.arenaapi.nms.common.util;

import com.google.common.collect.Sets;
import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.arenaapi.nms.common.world.BlockSource;
import io.github.zap.commons.vectors.Vector3I;
import io.github.zap.commons.vectors.Vectors;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class BoundedBlockIteratorTest {
    private static final BlockSource nullSrc = (x, y, z) -> null;
    private static final BlockSource mockSrc = ((x, y, z) -> {
        BlockCollisionView mockView = Mockito.mock(BlockCollisionView.class);
        Mockito.when(mockView.x()).thenReturn(x);
        Mockito.when(mockView.y()).thenReturn(y);
        Mockito.when(mockView.z()).thenReturn(z);
        return mockView;
    });

    private int length(Iterator<?> iterator) {
        int i = 0;
        while(iterator.hasNext()) {
            iterator.next();
            i++;
        }

        return i;
    }

    @Test
    void countSingleBlock() {
        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(nullSrc, new BoundingBox(0, 0, 0,
                1, 1, 1));

        int i = length(iterator);
        Assertions.assertSame(1, i);
    }

    @Test
    void countFourBlocks() {
        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(nullSrc, new BoundingBox(0, 0, 0,
                1, 1, 1).shift(-0.5, 0, -0.5));

        int i = length(iterator);
        Assertions.assertSame(4, i);
    }

    @Test
    void countEightBlocks() {
        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(nullSrc, new BoundingBox(0, 0, 0,
                1, 1, 1).shift(-0.5, -0.5, -0.5));

        int i = length(iterator);
        Assertions.assertSame(8, i);
    }

    @Test
    void doubleCoordinates() {
        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(mockSrc, new BoundingBox(0, 0, 0,
                2, 0, 0));

        Set<Vector3I> expected = Sets.newHashSet(Vectors.of(0, 0,  0), Vectors.of(1, 0, 0));
        Set<Vector3I> actual = new HashSet<>();
        while(iterator.hasNext()) {
            BlockCollisionView view = iterator.next();
            actual.add(Vectors.of(view.x(), view.y(), view.z()));
        }

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void quadrupleCoordinates() {
        Iterator<BlockCollisionView> iterator = new BoundedBlockIterator(mockSrc, new BoundingBox(0, 0, 0,
                2, 0, 2));

        Set<Vector3I> expected = Sets.newHashSet(Vectors.of(0, 0,  0), Vectors.of(1, 0, 0),
                Vectors.of(1, 0,  1), Vectors.of(0, 0, 1));
        Set<Vector3I> actual = new HashSet<>();
        while(iterator.hasNext()) {
            BlockCollisionView view = iterator.next();
            actual.add(Vectors.of(view.x(), view.y(), view.z()));
        }

        Assertions.assertEquals(expected, actual);
    }
}
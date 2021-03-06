package io.github.zap.arenaapi.nms.v1_16_R3.world;

import io.github.zap.arenaapi.nms.common.world.BlockCollisionView;
import io.github.zap.commons.vectors.graph.ArrayChunkGraph;
import io.github.zap.commons.vectors.graph.ChunkGraph;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

class CollisionChunkSnapshot_v1_16_R3 extends CollisionChunkAbstract_v1_16_R3 {
    private static final DataPaletteBlock<IBlockData> EMPTY_BLOCK_IDS =
            new ChunkSection(0, null, null, true).getBlocks();

    private static final Predicate<IBlockData> NON_SOLID_OR_PARTIAL = blockData -> {
        if(!blockData.getMaterial().isSolid()) {
            return true;
        }

        VoxelShape shape = shapeFromData(blockData);
        return (shape != VoxelShapes.fullCube() && shape != VoxelShapes.empty());
    };

    private static final IBlockData AIR_BLOCK_DATA = Blocks.AIR.getBlockData();

    private final DataPaletteBlock<IBlockData>[] palette;
    private final ChunkGraph<BlockCollisionView> nonSolidOrPartial = new ArrayChunkGraph<>(0, 0, 1, 1);
    private final int captureTick;

    CollisionChunkSnapshot_v1_16_R3(@NotNull Chunk chunk) {
        super(chunk.locX, chunk.locZ);

        palette = loadFromChunk(chunk);
        captureTick = Bukkit.getCurrentTick();
    }

    @Override
    public BlockCollisionView collisionView(int chunkX, int chunkY, int chunkZ) {
        assertValidChunkCoordinate(chunkX, chunkY, chunkZ);
        BlockCollisionView snapshot = nonSolidOrPartial.elementAt(chunkX, chunkY, chunkZ);

        if(snapshot == null) {
            IBlockData data = palette[chunkY >> 4].a(chunkX, chunkY & 15, chunkZ);

            snapshot = BlockCollisionView.from(originX + chunkX, chunkY, originZ + chunkZ,
                    data.createCraftBlockData(), new VoxelShapeWrapper_v1_16_R3(shapeFromData(data)));
        }

        return snapshot;
    }

    private DataPaletteBlock<IBlockData>[] loadFromChunk(net.minecraft.server.v1_16_R3.Chunk chunk) {
        ChunkSection[] sections = chunk.getSections();
        //noinspection unchecked
        DataPaletteBlock<IBlockData>[] sectionBlockIDs = new DataPaletteBlock[sections.length];

        for(int i = 0; i < sections.length; ++i) {
            ChunkSection section = sections[i];

            if (section == null) {
                sectionBlockIDs[i] = EMPTY_BLOCK_IDS;
            } else {
                NBTTagCompound data = new NBTTagCompound();
                section.getBlocks().a(data, "Palette", "BlockStates"); //fill up nbt data for this segment

                DataPaletteBlock<IBlockData> blocks = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE,
                        Block.REGISTRY_ID, GameProfileSerializer::c, GameProfileSerializer::a, AIR_BLOCK_DATA,
                        null, false);

                //load nbt data to palette
                blocks.a(data.getList("Palette", 10), data.getLongArray("BlockStates"));
                sectionBlockIDs[i] = blocks;

                int yOffset = section.getYPosition();
                BlockPosition.MutableBlockPosition examine = BlockPosition.ZERO.i();

                if(blocks.contains(NON_SOLID_OR_PARTIAL)) {
                    blocks.forEachLocation((blockData, position) -> {
                        if(!blockData.isAir()) {
                            /*
                            bit operator magic:
                            x & (2^n - 1) == x % (2^n) for all positive integer values of n
                            x >> n == x / 2^n for all positive integer n

                            translation:
                            int x = position % 16;
                            int y = (position / 256) + yOffset;
                            int z = (position % 256) / 16;

                            these coordinates are chunk-relative
                             */
                            int x = position & 15;
                            int y = (position >> 8) + yOffset;
                            int z = (position & 255) >> 4;

                            examine.d(x, y, z);

                            VoxelShape shape = blockData.getCollisionShape(chunk, examine);
                            if(!blockData.getMaterial().isSolid() || (shape != VoxelShapes.empty() && shape != VoxelShapes.fullCube())) {
                                nonSolidOrPartial.putElement(x, y, z, BlockCollisionView.from(originX + x, y,
                                        originZ + z, blockData.createCraftBlockData(), new VoxelShapeWrapper_v1_16_R3(shape)));
                            }
                        }
                    });
                }
            }
        }

        return sectionBlockIDs;
    }

    private static VoxelShape shapeFromData(IBlockData data) {
        return data.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
    }

    @Override
    public int captureTick() {
        return captureTick;
    }
}

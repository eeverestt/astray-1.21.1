package com.everest.astray.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class IslandChunkGenerator extends ChunkGenerator {
    public static final MapCodec<IslandChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
            ).apply(instance, IslandChunkGenerator::new)
    );

    public IslandChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        long seed = region.getSeed();

        int chunkX = pos.x;
        int chunkZ = pos.z;

        if (!hasCore(seed, chunkX, chunkZ)) return;

        for (int y = -60; y <= 320; y += 40) {
            generateLayer(region, seed, chunkX, chunkZ, y);
        }
    }

    private void generateLayer(ChunkRegion region, long seed, int chunkX, int chunkZ, int y) {
        int worldX = (chunkX << 4) + 8;
        int worldZ = (chunkZ << 4) + 8;
        BlockPos center = new BlockPos(worldX, y, worldZ);

        region.setBlockState(center, Blocks.RED_WOOL.getDefaultState(), Block.NOTIFY_LISTENERS);

        for (Direction dir : Direction.Type.HORIZONTAL) {
            int nx = chunkX + dir.getOffsetX();
            int nz = chunkZ + dir.getOffsetZ();
            if (hasCore(seed, nx, nz)) {
                connectChunks(region, y, chunkX, chunkZ, nx, nz);
            }
        }
    }

    private static boolean hasCore(long worldSeed, int chunkX, int chunkZ) {
        Random random = new Random(worldSeed ^ (chunkX * 341873128712L + chunkZ * 132897987541L));
        return random.nextFloat() < 0.1f; // 10% chance per chunk
    }

    private static void connectChunks(ChunkRegion region, int y, int x1, int z1, int x2, int z2) {
        int startX = (x1 << 4) + 8;
        int startZ = (z1 << 4) + 8;
        int endX = (x2 << 4) + 8;
        int endZ = (z2 << 4) + 8;

        int steps = 16;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.round(MathHelper.lerp(t, startX, endX));
            int z = (int) Math.round(MathHelper.lerp(t, startZ, endZ));
            region.setBlockState(new BlockPos(x, y, z), Blocks.RED_WOOL.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {}

    @Override
    public int getWorldHeight() {
        return 384;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 64;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(-64, new net.minecraft.block.BlockState[0]);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
        text.add("IslandChunkGenerator: multi-layer wool networks");
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {}
}

package com.glowingmemory.world;

import java.util.Random;

public class TerrainGenerator {
    private final long seed;
    private final SimplexNoise noise;
    private final Random random;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new SimplexNoise(seed);
        this.random = new Random(seed);
    }

    public void generate(Chunk chunk) {
        random.setSeed(seed ^ (chunk.getChunkX() * 341873128712L) ^ (chunk.getChunkZ() * 132897987541L));
        int baseX = chunk.getChunkX() * Chunk.SIZE;
        int baseZ = chunk.getChunkZ() * Chunk.SIZE;
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                float height = sampleHeight(worldX, worldZ);
                int h = (int) height;
                int surfaceY = Math.max(0, h - 1);
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    Block block = Block.AIR;
                    if (y < h - 3) block = Block.STONE;
                    else if (y < h - 1) block = Block.DIRT;
                    else if (y < h) block = Block.GRASS;
                    if (block != Block.AIR) {
                        chunk.setBlock(x, y, z, block);
                    }
                }
                boolean treePlaced = false;
                if (h > 4 && Math.abs(noise.noise(worldX * 0.05, worldZ * 0.05)) > 0.7 && random.nextFloat() < 0.05f) {
                    buildTree(chunk, x, h, z);
                    treePlaced = true;
                }
                if (!treePlaced) {
                    scatterFoliage(chunk, x, surfaceY, z, worldX, worldZ);
                }
            }
        }
    }

    private float sampleHeight(int x, int z) {
        double n = noise.noise(x * 0.01, z * 0.01) * 12;
        double n2 = noise.noise(x * 0.05, z * 0.05) * 3;
        return 48 + (float) (n + n2);
    }

    private void buildTree(Chunk chunk, int x, int h, int z) {
        int height = 4 + new Random(x * 31L + z * 17L).nextInt(3);
        for (int i = 0; i < height; i++) {
            if (h + i < Chunk.HEIGHT)
                chunk.setBlock(x, h + i, z, Block.WOOD);
        }
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = height - 2; dy <= height + 1; dy++) {
                    int bx = x + dx;
                    int by = h + dy;
                    int bz = z + dz;
                    if (bx >= 0 && bx < Chunk.SIZE && bz >= 0 && bz < Chunk.SIZE && by < Chunk.HEIGHT) {
                        double dist = Math.sqrt(dx * dx + dz * dz + (dy - height) * 0.6);
                        if (dist <= radius + 0.2) {
                            chunk.setBlock(bx, by, bz, Block.LEAVES);
                        }
                    }
                }
            }
        }
    }

    private void scatterFoliage(Chunk chunk, int x, int surfaceY, int z, int worldX, int worldZ) {
        int plantY = surfaceY + 1;
        if (plantY >= Chunk.HEIGHT) return;
        if (chunk.getBlock(x, surfaceY, z) != Block.GRASS) return;
        if (chunk.getBlock(x, plantY, z) != Block.AIR) return;
        float noiseMask = (float) (noise.noise(worldX * 0.12, worldZ * 0.12) * 0.5 + 0.5);
        if (random.nextFloat() < 0.22f * noiseMask) {
            chunk.setBlock(x, plantY, z, Block.TALL_GRASS);
        }
    }
}

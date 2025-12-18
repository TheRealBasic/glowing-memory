package com.glowingmemory.world;

import java.util.Random;

public class TerrainGenerator {
    private final SimplexNoise noise;

    public TerrainGenerator(long seed) {
        this.noise = new SimplexNoise(seed);
    }

    public void generate(Chunk chunk) {
        int baseX = chunk.getChunkX() * Chunk.SIZE;
        int baseZ = chunk.getChunkZ() * Chunk.SIZE;
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                float height = sampleHeight(worldX, worldZ);
                int h = (int) height;
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    Block block = Block.AIR;
                    if (y < h - 3) block = Block.STONE;
                    else if (y < h - 1) block = Block.DIRT;
                    else if (y < h) block = Block.GRASS;
                    if (block != Block.AIR) {
                        chunk.setBlock(x, y, z, block);
                    }
                }
                // Tree sprinkle
                if (h > 4 && Math.abs(noise.noise(worldX * 0.05, worldZ * 0.05)) > 0.7 && Math.random() < 0.05) {
                    buildTree(chunk, x, h, z);
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
}

package com.glowingmemory.world;

import com.glowingmemory.world.mesh.ChunkMesh;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 128;

    private final int chunkX;
    private final int chunkZ;
    private final Block[] blocks;
    private boolean dirty = true;
    private ChunkMesh mesh;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new Block[SIZE * HEIGHT * SIZE];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = Block.AIR;
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT || x < 0 || x >= SIZE || z < 0 || z >= SIZE) return Block.AIR;
        return blocks[(y * SIZE + z) * SIZE + x];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (y < 0 || y >= HEIGHT || x < 0 || x >= SIZE || z < 0 || z >= SIZE) return;
        blocks[(y * SIZE + z) * SIZE + x] = block;
        dirty = true;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() { dirty = true; }

    public void clean() {
        dirty = false;
    }

    public Block[] getBlocks() {
        return blocks;
    }

    public ChunkMesh getMesh() {
        return mesh;
    }

    public void setMesh(ChunkMesh mesh) {
        this.mesh = mesh;
    }
}

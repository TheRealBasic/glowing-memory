package com.glowingmemory.world.mesh;

import com.glowingmemory.world.Block;
import com.glowingmemory.world.Chunk;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class Mesher {
    private static final int[] FACE_OFFSETS = {
            1, 0, 0,
            -1, 0, 0,
            0, 1, 0,
            0, -1, 0,
            0, 0, 1,
            0, 0, -1
    };

    private static final float[][] FACE_VERTS = {
            {1,0,0, 1,1,0, 1,1,1, 1,0,1}, // +X
            {0,0,1, 0,1,1, 0,1,0, 0,0,0}, // -X
            {0,1,1, 1,1,1, 1,1,0, 0,1,0}, // +Y
            {0,0,0, 1,0,0, 1,0,1, 0,0,1}, // -Y
            {0,0,1, 1,0,1, 1,1,1, 0,1,1}, // +Z
            {0,1,0, 1,1,0, 1,0,0, 0,0,0}  // -Z
    };

    private static final float[][] FACE_NORMALS = {
            {1, 0, 0},  // +X
            {-1, 0, 0}, // -X
            {0, 1, 0},  // +Y
            {0, -1, 0}, // -Y
            {0, 0, 1},  // +Z
            {0, 0, -1}  // -Z
    };

    private static final int[] INDICES = {0,1,2, 2,3,0};

    public static ChunkMesh buildMesh(Chunk chunk, NeighborLookup lookup) {
        int visibleFaces = 0;
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == Block.AIR) continue;
                    for (int face = 0; face < 6; face++) {
                        int ox = x + FACE_OFFSETS[face * 3];
                        int oy = y + FACE_OFFSETS[face * 3 + 1];
                        int oz = z + FACE_OFFSETS[face * 3 + 2];
                        Block neighbor = lookup.getBlock(ox, oy, oz);
                        if (neighbor == Block.AIR || (neighbor == Block.WATER && block != Block.WATER)) {
                            visibleFaces++;
                        }
                    }
                }
            }
        }

        if (visibleFaces == 0) return null;

        FloatBuffer buffer = MemoryUtil.memAllocFloat(visibleFaces * INDICES.length * 9);
        int vertexCount = 0;
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == Block.AIR) continue;
                    for (int face = 0; face < 6; face++) {
                        int ox = x + FACE_OFFSETS[face * 3];
                        int oy = y + FACE_OFFSETS[face * 3 + 1];
                        int oz = z + FACE_OFFSETS[face * 3 + 2];
                        Block neighbor = lookup.getBlock(ox, oy, oz);
                        if (neighbor == Block.AIR || (neighbor == Block.WATER && block != Block.WATER)) {
                            Vector3f color = block.color;
                            float[] verts = FACE_VERTS[face];
                            float[] normal = FACE_NORMALS[face];
                            for (int i = 0; i < INDICES.length; i++) {
                                int idx = INDICES[i] * 3;
                                buffer.put(x + verts[idx]);
                                buffer.put(y + verts[idx + 1]);
                                buffer.put(z + verts[idx + 2]);
                                buffer.put(normal[0]);
                                buffer.put(normal[1]);
                                buffer.put(normal[2]);
                                buffer.put(color.x);
                                buffer.put(color.y);
                                buffer.put(color.z);
                            }
                            vertexCount += 6;
                        }
                    }
                }
            }
        }
        buffer.flip();
        if (vertexCount == 0) {
            MemoryUtil.memFree(buffer);
            return null;
        }
        int vao = GL30.glGenVertexArrays();
        int vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 9 * Float.BYTES, 0);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(buffer);
        return new ChunkMesh(vao, vbo, vertexCount);
    }

    public interface NeighborLookup {
        Block getBlock(int x, int y, int z);
    }
}

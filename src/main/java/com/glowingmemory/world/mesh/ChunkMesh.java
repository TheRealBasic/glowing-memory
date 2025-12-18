package com.glowingmemory.world.mesh;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class ChunkMesh {
    private final int vao;
    private final int vbo;
    private final int vertexCount;

    public ChunkMesh(int vao, int vbo, int vertexCount) {
        this.vao = vao;
        this.vbo = vbo;
        this.vertexCount = vertexCount;
    }

    public int getVao() { return vao; }
    public int getVertexCount() { return vertexCount; }

    public void destroy() {
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
    }
}

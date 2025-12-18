package com.glowingmemory.gfx;

import com.glowingmemory.player.Player;
import com.glowingmemory.world.Block;
import com.glowingmemory.world.Chunk;
import com.glowingmemory.world.World;
import com.glowingmemory.world.mesh.ChunkMesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.Collection;

public class Renderer {
    private Shader chunkShader;
    private Shader uiShader;
    private TextRenderer textRenderer;
    private int lineVao;
    private int lineVbo;

    public void init() {
        chunkShader = new Shader("shaders/chunk.vert", "shaders/chunk.frag");
        uiShader = new Shader("shaders/ui.vert", "shaders/ui.frag");
        textRenderer = new TextRenderer();
        lineVao = GL30.glGenVertexArrays();
        lineVbo = GL15.glGenBuffers();
    }

    public void render(World world, Player player) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, world.isWireframe() ? GL11.GL_LINE : GL11.GL_FILL);
        renderChunks(world, player);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        renderUI(world, player);
    }

    private void renderChunks(World world, Player player) {
        chunkShader.bind();
        Matrix4f proj = player.getCamera().getProjectionMatrix(getAspect());
        Matrix4f view = player.getCamera().getViewMatrix();
        int projLoc = GL20.glGetUniformLocation(chunkShader.getProgramId(), "projection");
        int viewLoc = GL20.glGetUniformLocation(chunkShader.getProgramId(), "view");
        GL20.glUniformMatrix4fv(projLoc, false, proj.get(new float[16]));
        GL20.glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));

        Collection<Chunk> renderChunks = world.getChunksToRender(player.getCamera().getPosition());
        for (Chunk chunk : renderChunks) {
            ChunkMesh mesh = chunk.getMesh();
            if (mesh == null) continue;
            Matrix4f model = new Matrix4f().translation(chunk.getChunkX() * Chunk.SIZE, 0, chunk.getChunkZ() * Chunk.SIZE);
            int modelLoc = GL20.glGetUniformLocation(chunkShader.getProgramId(), "model");
            GL20.glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));
            GL30.glBindVertexArray(mesh.getVao());
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.getVertexCount());
        }
        GL30.glBindVertexArray(0);
        chunkShader.unbind();
    }

    private void renderUI(World world, Player player) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawCrosshair(world);
        drawDebug(world, player);
        drawMenu(world, player);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawCrosshair(World world) {
        float[] verts = {
                -6, 0,
                6, 0,
                0, -6,
                0, 6
        };
        GL30.glBindVertexArray(lineVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lineVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verts, GL15.GL_STREAM_DRAW);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        uiShader.bind();
        Matrix4f proj = new Matrix4f().ortho(-getAspect(), getAspect(), 1, -1, -1, 1);
        int projLoc = GL20.glGetUniformLocation(uiShader.getProgramId(), "projection");
        GL20.glUniformMatrix4fv(projLoc, false, proj.get(new float[16]));
        GL11.glDrawArrays(GL11.GL_LINES, 0, 4);
        uiShader.unbind();
        GL20.glDisableVertexAttribArray(0);
    }

    private void drawDebug(World world, Player player) {
        if (!player.isDebugOpen()) return;
        int width = getWidth();
        int height = getHeight();
        Vector3f pos = player.getCamera().getPosition();
        String text = String.format("XYZ: %.2f %.2f %.2f | Chunk: %d %d | View: %d", pos.x, pos.y, pos.z,
                (int) Math.floor(pos.x / Chunk.SIZE), (int) Math.floor(pos.z / Chunk.SIZE), world.getViewDistance());
        textRenderer.drawText(text, 16, 32, 1f, width, height);
        textRenderer.drawText("FOV: " + player.getCamera().getFov(), 16, 56, 1f, width, height);
    }

    private void drawMenu(World world, Player player) {
        if (!player.isMenuOpen()) return;
        int width = getWidth();
        int height = getHeight();
        textRenderer.drawText("Spawn Menu (Q to close)", 16, height / 2f - 120, 1.2f, width, height);
        textRenderer.drawText("1-9 select block | Left click break | Right click place", 16, height / 2f - 92, 1f, width, height);
        textRenderer.drawText("E: View distance (" + world.getViewDistance() + ")  F: Wireframe(" + world.isWireframe() + ")", 16, height / 2f - 68, 1f, width, height);
        textRenderer.drawText("Z/X: Adjust FOV  F3: Debug  ESC: Quit", 16, height / 2f - 44, 1f, width, height);

        float startY = height / 2f;
        for (int i = 1; i < Block.values().length && i <= 9; i++) {
            Block block = Block.values()[i];
            String label = String.format("%d: %s%s", i, block.name(), player.getSelectedSlot() == i ? " <-" : "");
            textRenderer.drawText(label, 32, startY + (i - 1) * 22, 1f, width, height);
        }
    }

    public void destroy() {
        chunkShader.destroy();
        uiShader.destroy();
        textRenderer.destroy();
    }

    public float getAspect() {
        return (float) getWidth() / getHeight();
    }

    public int getWidth() {
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        return viewport[2];
    }

    public int getHeight() {
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        return viewport[3];
    }
}

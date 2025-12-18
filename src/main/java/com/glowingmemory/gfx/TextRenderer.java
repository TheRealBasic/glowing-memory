package com.glowingmemory.gfx;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextRenderer {
    private final Shader shader;
    private final int vao;
    private final int vbo;
    private final Map<Character, boolean[]> glyphs = new HashMap<>();

    public TextRenderer() {
        shader = new Shader("shaders/text.vert", "shaders/text.frag");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        buildFont();
    }

    private void buildFont() {
        // 5x7 font definitions using '#' for filled pixels
        add("A", new String[]{" ### ","#   #","#   #","#####","#   #","#   #","#   #"});
        add("B", new String[]{"#### ","#   #","#### ","#   #","#   #","#   #","#### "});
        add("C", new String[]{" ### ","#   #","#    ","#    ","#    ","#   #"," ### "});
        add("D", new String[]{"#### ","#   #","#   #","#   #","#   #","#   #","#### "});
        add("E", new String[]{"#####","#    ","#### ","#    ","#    ","#    ","#####"});
        add("F", new String[]{"#####","#    ","#### ","#    ","#    ","#    ","#    "});
        add("G", new String[]{" ### ","#   #","#    ","# ###","#   #","#   #"," ### "});
        add("H", new String[]{"#   #","#   #","#####","#   #","#   #","#   #","#   #"});
        add("I", new String[]{" ### ","  #  ","  #  ","  #  ","  #  ","  #  "," ### "});
        add("J", new String[]{"#####","   # ","   # ","   # ","   # ","#  # "," ##  "});
        add("K", new String[]{"#   #","#  # ","###  ","##   ","###  ","#  # ","#   #"});
        add("L", new String[]{"#    ","#    ","#    ","#    ","#    ","#    ","#####"});
        add("M", new String[]{"#   #","## ##","# # #","#   #","#   #","#   #","#   #"});
        add("N", new String[]{"#   #","##  #","# # #","#  ##","#   #","#   #","#   #"});
        add("O", new String[]{" ### ","#   #","#   #","#   #","#   #","#   #"," ### "});
        add("P", new String[]{"#### ","#   #","#   #","#### ","#    ","#    ","#    "});
        add("Q", new String[]{" ### ","#   #","#   #","#   #","# # #","#  # "," ## #"});
        add("R", new String[]{"#### ","#   #","#   #","#### ","# #  ","#  # ","#   #"});
        add("S", new String[]{" ####","#    ","#    "," ### ","    #","    #","#### "});
        add("T", new String[]{"#####","  #  ","  #  ","  #  ","  #  ","  #  ","  #  "});
        add("U", new String[]{"#   #","#   #","#   #","#   #","#   #","#   #"," ### "});
        add("V", new String[]{"#   #","#   #","#   #","#   #","#   #"," # # ","  #  "});
        add("W", new String[]{"#   #","#   #","#   #","# # #","# # #","## ##","#   #"});
        add("X", new String[]{"#   #","#   #"," # # ","  #  "," # # ","#   #","#   #"});
        add("Y", new String[]{"#   #","#   #"," # # ","  #  ","  #  ","  #  ","  #  "});
        add("Z", new String[]{"#####","    #","   # ","  #  "," #   ","#    ","#####"});
        add("0", new String[]{" ### ","#   #","#  ##","# # #","##  #","#   #"," ### "});
        add("1", new String[]{"  #  "," ##  ","# #  ","  #  ","  #  ","  #  ","#####"});
        add("2", new String[]{" ### ","#   #","    #","   # ","  #  "," #   ","#####"});
        add("3", new String[]{" ### ","#   #","    #","  ## ","    #","#   #"," ### "});
        add("4", new String[]{"   # ","  ## "," # # ","#  # ","#####","   # ","   # "});
        add("5", new String[]{"#####","#    ","#    ","#### ","    #","#   #"," ### "});
        add("6", new String[]{" ### ","#   #","#    ","#### ","#   #","#   #"," ### "});
        add("7", new String[]{"#####","    #","   # ","  #  ","  #  ","  #  ","  #  "});
        add("8", new String[]{" ### ","#   #","#   #"," ### ","#   #","#   #"," ### "});
        add("9", new String[]{" ### ","#   #","#   #"," ####","    #","#   #"," ### "});
        add("-", new String[]{"     ","     ","     "," ### ","     ","     ","     "});
        add("(", new String[]{"   # ","  #  ","  #  ","  #  ","  #  ","  #  ","   # "});
        add(")", new String[]{" #   ","  #  ","  #  ","  #  ","  #  ","  #  "," #   "});
        add(":", new String[]{"     ","  #  ","     ","     ","     ","  #  ","     "});
        add(".", new String[]{"     ","     ","     ","     ","     ","  #  ","     "});
        add("/", new String[]{"    #","   # ","   # ","  #  ","  #  "," #   ","#    "});
        add(" ", new String[]{"     ","     ","     ","     ","     ","     ","     "});
        add("|", new String[]{"  #  ","  #  ","  #  ","  #  ","  #  ","  #  ","  #  "});
    }

    private void add(String character, String[] pattern) {
        boolean[] data = new boolean[5 * 7];
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 5; x++) {
                data[y * 5 + x] = pattern[y].charAt(x) != ' ';
            }
        }
        glyphs.put(character.charAt(0), data);
    }

    public void drawText(String text, float x, float y, float scale, int windowWidth, int windowHeight) {
        shader.bind();
        Matrix4f proj = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            proj.get(fb);
            int projLoc = GL20.glGetUniformLocation(shader.getProgramId(), "projection");
            GL20.glUniformMatrix4fv(projLoc, false, fb);
        }
        int colorLoc = GL20.glGetUniformLocation(shader.getProgramId(), "color");
        GL20.glUniform3f(colorLoc, 1f, 1f, 1f);

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);

        float cx = x;
        float cy = y;
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            boolean[] glyph = glyphs.getOrDefault(c, glyphs.get(' '));
            FloatBuffer buffer = BufferUtils.createFloatBuffer(6 * 2 * 5 * 7);
            for (int py = 0; py < 7; py++) {
                for (int px = 0; px < 5; px++) {
                    if (!glyph[py * 5 + px]) continue;
                    float x0 = cx + px * scale;
                    float y0 = cy + py * scale;
                    float x1 = x0 + scale;
                    float y1 = y0 + scale;
                    buffer.put(new float[]{x0, y0, x1, y0, x1, y1});
                    buffer.put(new float[]{x0, y0, x1, y1, x0, y1});
                }
            }
            buffer.flip();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
            GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 2 * Float.BYTES, 0);
            int verts = buffer.limit() / 2;
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, verts);
            cx += 6 * scale;
        }

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    public void destroy() {
        shader.destroy();
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
    }
}

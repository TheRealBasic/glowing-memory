package com.glowingmemory.gfx;

import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Shader {
    private final int programId;

    public Shader(String vertexPath, String fragmentPath) {
        try {
            String vertexSrc = loadResource(vertexPath);
            String fragmentSrc = loadResource(fragmentPath);
            int vertex = compile(GL20.GL_VERTEX_SHADER, vertexSrc);
            int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentSrc);
            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vertex);
            GL20.glAttachShader(programId, fragment);
            GL20.glLinkProgram(programId);
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
                throw new RuntimeException("Failed to link shader: " + GL20.glGetProgramInfoLog(programId));
            }
            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String loadResource(String path) throws IOException {
        try (InputStream is = Shader.class.getClassLoader().getResourceAsStream(path.replace("src/main/resources/", ""))) {
            if (is == null) throw new IOException("Missing resource: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader compile error: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() { GL20.glUseProgram(0); }

    public void destroy() { GL20.glDeleteProgram(programId); }

    public int getProgramId() { return programId; }
}

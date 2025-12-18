package com.glowingmemory.gfx;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Window {
    private final int width;
    private final int height;
    private final String title;
    private long handle;
    private boolean resized;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwSetFramebufferSizeCallback(handle, (win, w, h) -> resized = true);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public void swapBuffers() {
        if (resized) {
            GL11.glViewport(0, 0, getWidth(), getHeight());
            resized = false;
        }
        GLFW.glfwSwapBuffers(handle);
    }

    public int getWidth() {
        int[] w = new int[1];
        GLFW.glfwGetFramebufferSize(handle, w, new int[1]);
        return w[0] > 0 ? w[0] : width;
    }

    public int getHeight() {
        int[] h = new int[1];
        GLFW.glfwGetFramebufferSize(handle, new int[1], h);
        return h[0] > 0 ? h[0] : height;
    }

    public long getHandle() {
        return handle;
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }
}

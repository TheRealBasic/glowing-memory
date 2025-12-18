package com.glowingmemory.input;

import org.lwjgl.glfw.GLFW;

public class Input {
    private static final int FIRST_KEY = GLFW.GLFW_KEY_SPACE;
    private static final int LAST_KEY = GLFW.GLFW_KEY_LAST;
    private static final boolean[] keys = new boolean[LAST_KEY + 1];
    private static final boolean[] pressed = new boolean[LAST_KEY + 1];
    private static final boolean[] mouseButtons = new boolean[8];
    private static final boolean[] mousePressed = new boolean[8];
    private static double mouseX, mouseY, lastMouseX, lastMouseY;
    private static double deltaX, deltaY;
    private static boolean firstMouse = true;

    public static void poll(long window) {
        for (int key = FIRST_KEY; key <= LAST_KEY; key++) {
            boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            pressed[key] = down && !keys[key];
            keys[key] = down;
        }
        for (int i = 0; i < mouseButtons.length; i++) {
            boolean down = GLFW.glfwGetMouseButton(window, i) == GLFW.GLFW_PRESS;
            mousePressed[i] = down && !mouseButtons[i];
            mouseButtons[i] = down;
        }
        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);
        mouseX = mx[0];
        mouseY = my[0];
        if (firstMouse) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstMouse = false;
        }
        deltaX = mouseX - lastMouseX;
        deltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public static void resetMouse(long window) {
        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);
        lastMouseX = mx[0];
        lastMouseY = my[0];
        deltaX = 0;
        deltaY = 0;
        firstMouse = true;
    }

    public static boolean isKeyDown(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }

    public static boolean isKeyPressed(int key) {
        return key >= 0 && key < pressed.length && pressed[key];
    }

    public static boolean isMousePressed(int button) {
        return button < mousePressed.length && mousePressed[button];
    }

    public static double getDeltaX() {
        return deltaX;
    }

    public static double getDeltaY() {
        return deltaY;
    }
}

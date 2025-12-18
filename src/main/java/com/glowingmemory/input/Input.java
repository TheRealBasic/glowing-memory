package com.glowingmemory.input;

import org.lwjgl.glfw.GLFW;

public class Input {
    private static final boolean[] keys = new boolean[350];
    private static final boolean[] pressed = new boolean[350];
    private static final boolean[] mouseButtons = new boolean[8];
    private static final boolean[] mousePressed = new boolean[8];
    private static double mouseX, mouseY, lastMouseX, lastMouseY;
    private static double deltaX, deltaY;
    private static boolean firstMouse = true;

    public static void poll(long window) {
        for (int i = 0; i < keys.length; i++) {
            boolean down = GLFW.glfwGetKey(window, i) == GLFW.GLFW_PRESS;
            pressed[i] = down && !keys[i];
            keys[i] = down;
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

    public static boolean isKeyDown(int key) {
        return keys[key];
    }

    public static boolean isKeyPressed(int key) {
        return pressed[key];
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

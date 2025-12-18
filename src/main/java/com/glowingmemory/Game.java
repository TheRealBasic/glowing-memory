package com.glowingmemory;

import com.glowingmemory.gfx.Renderer;
import com.glowingmemory.gfx.Window;
import com.glowingmemory.input.Input;
import com.glowingmemory.player.Camera;
import com.glowingmemory.player.Player;
import com.glowingmemory.world.World;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class Game implements Runnable {
    private static final int TARGET_FPS = 60;
    private static final double TIME_STEP = 1.0 / TARGET_FPS;

    private final Window window;
    private final Renderer renderer;
    private final World world;
    private final Player player;

    private boolean running = true;

    public Game() {
        window = new Window(1280, 720, "Glowing Memory - Voxel Sandbox");
        renderer = new Renderer();
        world = new World(renderer);
        Camera camera = new Camera();
        player = new Player(camera, world);
    }

    @Override
    public void run() {
        window.init();
        renderer.init();
        world.init();
        player.init(window);
        GL11.glViewport(0, 0, window.getWidth(), window.getHeight());

        double previous = GLFW.glfwGetTime();
        double lag = 0.0;

        while (!window.shouldClose() && running) {
            double current = GLFW.glfwGetTime();
            double elapsed = current - previous;
            previous = current;
            lag += elapsed;

            Input.poll(window.getHandle());
            while (lag >= TIME_STEP) {
                update((float) TIME_STEP);
                lag -= TIME_STEP;
            }
            render();
            window.swapBuffers();
            GLFW.glfwPollEvents();
        }

        shutdown();
    }

    private void update(float dt) {
        if (Input.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            running = false;
        }
        player.update(dt, window);
        world.update(player);
    }

    private void render() {
        GL11.glClearColor(0.6f, 0.8f, 1.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderer.render(world, player);
    }

    private void shutdown() {
        world.saveAll();
        renderer.destroy();
        window.destroy();
    }

    public static void main(String[] args) {
        new Game().run();
    }
}

package com.glowingmemory.player;

import com.glowingmemory.gfx.Window;
import com.glowingmemory.input.Input;
import com.glowingmemory.world.Block;
import com.glowingmemory.world.Raycaster;
import com.glowingmemory.world.World;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Player {
    private final Camera camera;
    private final World world;
    private final Vector3f velocity = new Vector3f();
    private boolean onGround = false;
    private int selectedSlot = 1;
    private boolean menuOpen = false;
    private boolean debugOpen = false;

    private static final float SPEED = 7f;
    private static final float SPRINT = 11f;
    private static final float GRAVITY = -28f;
    private static final float JUMP = 9f;

    public Player(Camera camera, World world) {
        this.camera = camera;
        this.world = world;
    }

    public void init(Window window) {
        GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void update(float dt, Window window) {
        handleMouseLook(window);
        handleMovement(dt, window);
        handleInteractions(window);
    }

    private void handleMouseLook(Window window) {
        if (menuOpen) return;
        float sensitivity = 0.12f;
        float dx = (float) Input.getDeltaX() * sensitivity;
        float dy = (float) Input.getDeltaY() * sensitivity;
        camera.setYaw(camera.getYaw() + dx);
        camera.setPitch(Math.max(-89f, Math.min(89f, camera.getPitch() - dy)));
    }

    private void handleMovement(float dt, Window window) {
        if (menuOpen) {
            velocity.zero();
            return;
        }
        Vector3f front = camera.getFront();
        Vector3f right = new Vector3f(front).cross(0, 1, 0).normalize();
        Vector3f move = new Vector3f();
        float speed = Input.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? SPRINT : SPEED;

        if (Input.isKeyDown(GLFW.GLFW_KEY_W)) move.add(front.x, 0, front.z);
        if (Input.isKeyDown(GLFW.GLFW_KEY_S)) move.add(-front.x, 0, -front.z);
        if (Input.isKeyDown(GLFW.GLFW_KEY_A)) move.add(-right.x, 0, -right.z);
        if (Input.isKeyDown(GLFW.GLFW_KEY_D)) move.add(right.x, 0, right.z);
        if (move.lengthSquared() > 0) {
            move.normalize().mul(speed);
        }
        velocity.x = move.x;
        velocity.z = move.z;

        velocity.y += GRAVITY * dt;
        if (Input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && onGround) {
            velocity.y = JUMP;
            onGround = false;
        }
        Vector3f position = camera.getPosition();
        Vector3f newPos = new Vector3f(position);
        newPos.add(velocity.x * dt, 0, velocity.z * dt);
        newPos = world.resolveCollision(position, new Vector3f(0.6f, 1.5f, 0.6f), newPos);
        position.set(newPos);

        newPos = new Vector3f(position).add(0, velocity.y * dt, 0);
        Vector3f verticalResolved = world.resolveCollision(position, new Vector3f(0.6f, 1.5f, 0.6f), newPos);
        if (verticalResolved.y != newPos.y) {
            if (velocity.y < 0) onGround = true;
            velocity.y = 0;
        }
        position.set(verticalResolved);
    }

    private void handleInteractions(Window window) {
        for (int i = 0; i < 9; i++) {
            if (Input.isKeyPressed(GLFW.GLFW_KEY_1 + i)) {
                selectedSlot = i + 1;
            }
        }
        if (Input.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            debugOpen = !debugOpen;
        }
        if (Input.isKeyPressed(GLFW.GLFW_KEY_Q)) {
            menuOpen = !menuOpen;
            GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR, menuOpen ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
            Input.resetMouse(window.getHandle());
        }
        if (menuOpen) return;

        if (Input.isKeyPressed(GLFW.GLFW_KEY_E)) {
            world.cycleViewDistance();
        }
        if (Input.isKeyPressed(GLFW.GLFW_KEY_Z)) {
            camera.adjustFov(-2f);
        }
        if (Input.isKeyPressed(GLFW.GLFW_KEY_X)) {
            camera.adjustFov(2f);
        }

        if (Input.isKeyPressed(GLFW.GLFW_KEY_F)) {
            world.toggleWireframe();
        }

        Raycaster.RayResult ray = Raycaster.raycast(camera.getPosition(), camera.getFront(), world, 6f);
        if (ray != null) {
            if (Input.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                world.setBlock(ray.hit, Block.AIR);
            }
            if (Input.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                if (ray.normal != null) {
                    int blockId = Math.min(selectedSlot, Block.values().length - 1);
                    world.setBlock(ray.hit.add(ray.normal, new Vector3f()), Block.values()[blockId]);
                }
            }
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public boolean isMenuOpen() {
        return menuOpen;
    }

    public boolean isDebugOpen() {
        return debugOpen;
    }
}

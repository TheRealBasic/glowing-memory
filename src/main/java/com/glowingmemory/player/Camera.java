package com.glowingmemory.player;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position = new Vector3f(0, 70, 0);
    private float pitch = 20f;
    private float yaw = -90f;
    private float fov = 70f;

    public Matrix4f getViewMatrix() {
        Vector3f front = getFront();
        Vector3f center = new Vector3f(position).add(front);
        Vector3f up = new Vector3f(0, 1, 0);
        return new Matrix4f().lookAt(position, center, up);
    }

    public Matrix4f getProjectionMatrix(float aspect) {
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, 0.1f, 512f);
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Vector3f getFront() {
        float x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float y = (float) Math.sin(Math.toRadians(pitch));
        float z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return new Vector3f(x, y, z).normalize();
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void adjustFov(float delta) {
        fov = Math.max(40f, Math.min(110f, fov + delta));
    }

    public float getFov() {
        return fov;
    }
}

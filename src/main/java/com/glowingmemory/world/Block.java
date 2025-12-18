package com.glowingmemory.world;

import org.joml.Vector3f;

public enum Block {
    AIR(false, new Vector3f(0, 0, 0)),
    GRASS(true, new Vector3f(0.34f, 0.7f, 0.25f)),
    DIRT(true, new Vector3f(0.5f, 0.3f, 0.15f)),
    STONE(true, new Vector3f(0.6f, 0.6f, 0.6f)),
    SAND(true, new Vector3f(0.9f, 0.85f, 0.6f)),
    WOOD(true, new Vector3f(0.55f, 0.35f, 0.2f)),
    LEAVES(true, new Vector3f(0.2f, 0.5f, 0.2f)),
    WATER(true, new Vector3f(0.2f, 0.4f, 0.8f)),
    TALL_GRASS(false, new Vector3f(0.35f, 0.75f, 0.28f), 0.65f);

    public final boolean solid;
    public final Vector3f color;
    public final float renderHeight;

    Block(boolean solid, Vector3f color) {
        this(solid, color, 1f);
    }

    Block(boolean solid, Vector3f color, float renderHeight) {
        this.solid = solid;
        this.color = color;
        this.renderHeight = renderHeight;
    }
}

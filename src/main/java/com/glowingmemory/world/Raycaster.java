package com.glowingmemory.world;

import org.joml.Vector3f;

public class Raycaster {
    public static class RayResult {
        public final Vector3f hit;
        public final Vector3f normal;
        public final Block block;

        public RayResult(Vector3f hit, Vector3f normal, Block block) {
            this.hit = hit;
            this.normal = normal;
            this.block = block;
        }
    }

    public static RayResult raycast(Vector3f origin, Vector3f dir, World world, float maxDistance) {
        Vector3f pos = new Vector3f(origin);
        Vector3f step = new Vector3f(dir).normalize().mul(0.1f);
        float traveled = 0f;
        while (traveled < maxDistance) {
            int bx = (int) Math.floor(pos.x);
            int by = (int) Math.floor(pos.y);
            int bz = (int) Math.floor(pos.z);
            Block block = world.getBlockGlobal(bx, by, bz);
            if (block != Block.AIR) {
                Vector3f normal = new Vector3f(dir).normalize().negate();
                return new RayResult(new Vector3f(bx, by, bz), normal, block);
            }
            pos.add(step);
            traveled += step.length();
        }
        return null;
    }
}

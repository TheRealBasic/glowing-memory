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
        Vector3f direction = new Vector3f(dir).normalize();
        Vector3f pos = new Vector3f(origin);

        int bx = (int) Math.floor(pos.x);
        int by = (int) Math.floor(pos.y);
        int bz = (int) Math.floor(pos.z);

        int stepX = Integer.compare((int) Math.signum(direction.x), 0);
        int stepY = Integer.compare((int) Math.signum(direction.y), 0);
        int stepZ = Integer.compare((int) Math.signum(direction.z), 0);

        float tDeltaX = stepX != 0 ? Math.abs(1f / direction.x) : Float.POSITIVE_INFINITY;
        float tDeltaY = stepY != 0 ? Math.abs(1f / direction.y) : Float.POSITIVE_INFINITY;
        float tDeltaZ = stepZ != 0 ? Math.abs(1f / direction.z) : Float.POSITIVE_INFINITY;

        float tMaxX = stepX > 0 ? ((bx + 1) - pos.x) * tDeltaX : (pos.x - bx) * tDeltaX;
        float tMaxY = stepY > 0 ? ((by + 1) - pos.y) * tDeltaY : (pos.y - by) * tDeltaY;
        float tMaxZ = stepZ > 0 ? ((bz + 1) - pos.z) * tDeltaZ : (pos.z - bz) * tDeltaZ;

        Vector3f lastNormal = null;
        float traveled = 0f;
        while (traveled <= maxDistance) {
            Block block = world.getBlockGlobal(bx, by, bz);
            if (block != Block.AIR) {
                return new RayResult(new Vector3f(bx, by, bz), lastNormal, block);
            }

            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                bx += stepX;
                traveled = tMaxX;
                tMaxX += tDeltaX;
                lastNormal = new Vector3f(-stepX, 0, 0);
            } else if (tMaxY < tMaxZ) {
                by += stepY;
                traveled = tMaxY;
                tMaxY += tDeltaY;
                lastNormal = new Vector3f(0, -stepY, 0);
            } else {
                bz += stepZ;
                traveled = tMaxZ;
                tMaxZ += tDeltaZ;
                lastNormal = new Vector3f(0, 0, -stepZ);
            }
        }
        return null;
    }
}

package com.glowingmemory.world;

import com.glowingmemory.gfx.Renderer;
import com.glowingmemory.player.Player;
import com.glowingmemory.util.Log;
import com.glowingmemory.world.mesh.ChunkMesh;
import com.glowingmemory.world.mesh.Mesher;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class World {
    private final Renderer renderer;
    private final Map<Long, Chunk> chunks = new HashMap<>();
    private final TerrainGenerator generator = new TerrainGenerator(42L);
    private int viewDistance = 4;
    private boolean wireframe = false;
    private final Path saveDir = Path.of("saves");

    public World(Renderer renderer) {
        this.renderer = renderer;
    }

    public void init() {
        try {
            Files.createDirectories(saveDir);
            Log.info("World save directory ensured at " + saveDir.toAbsolutePath());
        } catch (IOException e) {
            Log.error("Failed to create save directory", e);
            throw new RuntimeException(e);
        }
    }

    public void update(Player player) {
        Vector3f pos = player.getCamera().getPosition();
        int chunkX = (int) Math.floor(pos.x / Chunk.SIZE);
        int chunkZ = (int) Math.floor(pos.z / Chunk.SIZE);
        for (int x = -viewDistance; x <= viewDistance; x++) {
            for (int z = -viewDistance; z <= viewDistance; z++) {
                getOrCreateChunk(chunkX + x, chunkZ + z);
            }
        }
        for (Chunk chunk : new ArrayList<>(chunks.values())) {
            if (chunk.isDirty()) {
                buildMesh(chunk);
            }
        }
    }

    private void buildMesh(Chunk chunk) {
        ChunkMesh old = chunk.getMesh();
        if (old != null) old.destroy();
        ChunkMesh mesh = Mesher.buildMesh(chunk, (x, y, z) -> getBlock(chunk, x, y, z));
        chunk.setMesh(mesh);
        chunk.clean();
    }

    private Block getBlock(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= Chunk.HEIGHT) return Block.AIR;

        int neighborChunkX = chunk.getChunkX();
        int neighborChunkZ = chunk.getChunkZ();

        if (x < 0) {
            neighborChunkX -= 1;
            x += Chunk.SIZE;
        } else if (x >= Chunk.SIZE) {
            neighborChunkX += 1;
            x -= Chunk.SIZE;
        }

        if (z < 0) {
            neighborChunkZ -= 1;
            z += Chunk.SIZE;
        } else if (z >= Chunk.SIZE) {
            neighborChunkZ += 1;
            z -= Chunk.SIZE;
        }

        if (neighborChunkX != chunk.getChunkX() || neighborChunkZ != chunk.getChunkZ()) {
            Chunk neighbor = getLoadedChunk(neighborChunkX, neighborChunkZ);
            return neighbor != null ? neighbor.getBlock(x, y, z) : Block.AIR;
        }

        return chunk.getBlock(x, y, z);
    }

    public Block getBlockGlobal(int x, int y, int z) {
        int cx = floorDiv(x, Chunk.SIZE);
        int cz = floorDiv(z, Chunk.SIZE);
        Chunk chunk = getOrCreateChunk(cx, cz);
        int lx = mod(x, Chunk.SIZE);
        int lz = mod(z, Chunk.SIZE);
        return chunk.getBlock(lx, y, lz);
    }

    public void setBlock(Vector3f worldPos, Block block) {
        int x = (int) Math.floor(worldPos.x);
        int y = (int) Math.floor(worldPos.y);
        int z = (int) Math.floor(worldPos.z);
        int cx = floorDiv(x, Chunk.SIZE);
        int cz = floorDiv(z, Chunk.SIZE);
        Chunk chunk = getOrCreateChunk(cx, cz);
        chunk.setBlock(mod(x, Chunk.SIZE), y, mod(z, Chunk.SIZE), block);
        chunk.clean();
        buildMesh(chunk);
    }

    public Vector3f resolveCollision(Vector3f currentPos, Vector3f halfSize, Vector3f target) {
        Vector3f resolved = new Vector3f(target);
        if (collides(resolved.x, currentPos.y, currentPos.z, halfSize)) {
            resolved.x = currentPos.x;
        }
        if (collides(resolved.x, currentPos.y, resolved.z, halfSize)) {
            resolved.z = currentPos.z;
        }
        if (collides(resolved.x, resolved.y, resolved.z, halfSize)) {
            resolved.y = currentPos.y;
        }
        return resolved;
    }

    private boolean collides(float x, float y, float z, Vector3f halfSize) {
        float halfX = halfSize.x / 2f;
        float halfY = halfSize.y / 2f;
        float halfZ = halfSize.z / 2f;
        int minX = (int) Math.floor(x - halfX);
        int maxX = (int) Math.floor(x + halfX);
        int minY = (int) Math.floor(y - halfY);
        int maxY = (int) Math.floor(y + halfY);
        int minZ = (int) Math.floor(z - halfZ);
        int maxZ = (int) Math.floor(z + halfZ);
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (getBlockGlobal(bx, by, bz).solid) return true;
                }
            }
        }
        return false;
    }

    public Chunk getOrCreateChunk(int x, int z) {
        long key = (((long) x) << 32) ^ (z & 0xffffffffL);
        return chunks.computeIfAbsent(key, k -> loadOrGenerate(x, z));
    }

    private Chunk getLoadedChunk(int x, int z) {
        long key = (((long) x) << 32) ^ (z & 0xffffffffL);
        return chunks.get(key);
    }

    private Chunk loadOrGenerate(int x, int z) {
        Chunk chunk = loadChunk(x, z);
        if (chunk != null) return chunk;
        chunk = new Chunk(x, z);
        generator.generate(chunk);
        Log.info("Generated new chunk at (" + x + ", " + z + ")");
        chunk.markDirty();
        saveChunk(chunk);
        return chunk;
    }

    public Collection<Chunk> getChunksToRender(Vector3f cameraPos) {
        List<Chunk> list = new ArrayList<>();
        int cx = (int) Math.floor(cameraPos.x / Chunk.SIZE);
        int cz = (int) Math.floor(cameraPos.z / Chunk.SIZE);
        for (Chunk chunk : chunks.values()) {
            int dx = chunk.getChunkX() - cx;
            int dz = chunk.getChunkZ() - cz;
            if (Math.abs(dx) <= viewDistance && Math.abs(dz) <= viewDistance) {
                list.add(chunk);
            }
        }
        return list;
    }

    public void saveAll() {
        for (Chunk chunk : chunks.values()) {
            saveChunk(chunk);
        }
    }

    private void saveChunk(Chunk chunk) {
        Path file = saveDir.resolve(chunk.getChunkX() + "_" + chunk.getChunkZ() + ".chk");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            for (Block block : chunk.getBlocks()) {
                out.writeByte(block.ordinal());
            }
            Log.info("Saved chunk to " + file.toAbsolutePath());
        } catch (IOException e) {
            Log.error("Failed to save chunk at (" + chunk.getChunkX() + ", " + chunk.getChunkZ() + ")", e);
        }
    }

    private Chunk loadChunk(int x, int z) {
        Path file = saveDir.resolve(x + "_" + z + ".chk");
        if (!Files.exists(file)) return null;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            int expectedSize = Chunk.SIZE * Chunk.SIZE * Chunk.HEIGHT;
            if (Files.size(file) != expectedSize) {
                Log.warn("Corrupted chunk file (unexpected size), regenerating: " + file.toAbsolutePath());
                Files.deleteIfExists(file);
                return null;
            }

            Chunk chunk = new Chunk(x, z);
            for (int i = 0; i < chunk.getBlocks().length; i++) {
                byte id = in.readByte();
                chunk.getBlocks()[i] = Block.values()[id];
            }
            chunk.markDirty();
            Log.info("Loaded chunk from " + file.toAbsolutePath());
            return chunk;
        } catch (EOFException e) {
            Log.warn("Corrupted chunk file (unexpected end of file), regenerating: " + file.toAbsolutePath());
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                Log.error("Failed to delete corrupted chunk file " + file.toAbsolutePath(), ex);
            }
            return null;
        } catch (IOException e) {
            Log.error("Failed to load chunk from " + file.toAbsolutePath(), e);
            return null;
        }
    }

    private int mod(int x, int m) {
        int r = x % m;
        return r < 0 ? r + m : r;
    }

    private int floorDiv(int x, int m) {
        return (int) Math.floor((double) x / m);
    }

    public int getViewDistance() { return viewDistance; }

    public boolean isWireframe() { return wireframe; }

    public void cycleViewDistance() { viewDistance = viewDistance >= 8 ? 2 : viewDistance + 1; }

    public void toggleWireframe() { wireframe = !wireframe; }

    public Renderer getRenderer() { return renderer; }
}

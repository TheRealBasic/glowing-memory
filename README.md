# Glowing Memory - Voxel Sandbox

A lightweight Java 17 sandbox in the spirit of Minecraft/Garry's Mod. Explore a procedurally generated voxel world, break/place blocks, and tweak settings from an in-game spawn menu.

## Requirements
- Java 17+
- Maven 3.x
- OpenGL 3.3 capable GPU

## Running
```bash
mvn -DskipTests exec:java
```
The first run will download LWJGL artifacts and create a `saves/` folder for world data.

## Controls
- **Mouse:** Look around (cursor captured). Click to break/place.
- **W/A/S/D:** Move. **Shift:** Sprint. **Space:** Jump.
- **1-9:** Select block type (grass, dirt, stone, sand, wood, leaves, water).
- **Q:** Toggle spawn menu overlay (releases mouse).
- **E:** Cycle view distance.
- **Z/X:** Narrow/Widen FOV.
- **F:** Toggle wireframe.
- **F3:** Debug overlay (FPS, position, chunk info).
- **ESC:** Quit.

## Features
- Infinite-style chunked world (16x16x128) with procedural terrain and scattered trees.
- Chunk streaming around the player, with per-chunk mesh buffers and face-culling meshing.
- Save/load of chunks to simple binary files in `saves/`.
- First-person controller with gravity, jumping, AABB collision, and raycast interaction.
- Crosshair, debug overlay, and spawn menu overlay with adjustable view distance and FOV.

## Notes
- If performance dips, reduce the view distance with **E** or enable wireframe with **F** for diagnostics.

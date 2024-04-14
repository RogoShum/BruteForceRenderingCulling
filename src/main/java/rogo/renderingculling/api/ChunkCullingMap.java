package rogo.renderingculling.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ChunkCullingMap extends CullingMap {
    private int renderDistance = 0;
    private int spacePartitionSize = 0;
    private boolean updateVisibleChunks = true;
    private Queue<BlockPos> visibleChunks = new ArrayDeque<>();
    private static final int[][] DIRECTIONS = {{0, 1, 0}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

    public int queueUpdateCount = 0;
    public int lastQueueUpdateCount = 0;

    public ChunkCullingMap(int width, int height) {
        super(width, height);
    }

    @Override
    int configDelayCount() {
        return Config.getDepthUpdateDelay();
    }

    @Override
    int bindFrameBufferId() {
        return CullingHandler.CHUNK_CULLING_MAP_TARGET.frameBufferId;
    }

    public void generateIndex(int renderDistance) {
        this.renderDistance = renderDistance;
        spacePartitionSize = 2 * renderDistance + 1;

    }

    public boolean isChunkOffsetCameraVisible(int x, int y, int z) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int cameraX = (int)camera.x >> 4;
        int cameraZ = (int)camera.z >> 4;

        if(y < 0)
            y -= 9;

        int chunkX = x >> 4;
        int chunkY = (int) (y * 0.0625) + CullingHandler.LEVEL_MIN_SECTION_ABS;
        int chunkZ = z >> 4;

        int posX = chunkX - cameraX;
        int posZ = chunkZ - cameraZ;

        return isChunkVisible(posX, chunkY, posZ);
    }

    public boolean isChunkVisible(int posX, int posY, int posZ) {
        int index = 1 + ((((posX + renderDistance) * spacePartitionSize * CullingHandler.LEVEL_HEIGHT_OFFSET + (posZ + renderDistance) * CullingHandler.LEVEL_HEIGHT_OFFSET + posY) << 2));

        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > 0;
        }

        return false;
    }

    @Override
    public void readData() {
        super.readData();
        updateVisibleChunks = true;
    }

    public void updateVisibleChunks() {
        if(updateVisibleChunks) {
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            int cameraX = (int)camera.x >> 4;
            int cameraY = (int)camera.y >> 4;
            int cameraZ = (int)camera.z >> 4;

            BlockPos origin = new BlockPos(cameraX, cameraY, cameraZ);
            if (origin.getY() < Minecraft.getInstance().level.getMinSection()) {
                origin = new BlockPos(cameraX, Minecraft.getInstance().level.getMinSection(), cameraZ);
            } else if (origin.getY() >= Minecraft.getInstance().level.getMaxSection()) {
                origin = new BlockPos(cameraX, Minecraft.getInstance().level.getMaxSection() - 1, cameraZ);
            }

            bfsSearch(origin);

            updateVisibleChunks = false;
            queueUpdateCount++;
        }
    }

    public Queue<BlockPos> getVisibleChunks() {
        if(visibleChunks.isEmpty()) {
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            int cameraX = (int)camera.x >> 4;
            int cameraY = (int)camera.y >> 4;
            int cameraZ = (int)camera.z >> 4;

            BlockPos origin = new BlockPos(cameraX, cameraY, cameraZ);
            if (origin.getY() < Minecraft.getInstance().level.getMinSection()) {
                origin = new BlockPos(cameraX, Minecraft.getInstance().level.getMinSection(), cameraZ);
            } else if (origin.getY() >= Minecraft.getInstance().level.getMaxSection()) {
                origin = new BlockPos(cameraX, Minecraft.getInstance().level.getMaxSection() - 1, cameraZ);
            }
            visibleChunks.add(origin);

            for (int[] direction : DIRECTIONS) {
                int newX = origin.getX() + direction[0];
                int newY = origin.getY() + direction[1];
                int newZ = origin.getZ() + direction[2];

                BlockPos neighborChunk = new BlockPos(newX, newY, newZ);
                visibleChunks.add(neighborChunk);
            }
        }
        return visibleChunks;
    }

    private void bfsSearch(BlockPos origin) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Queue<BlockPos> visible = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        visited.add(BlockPos.ZERO);
        queue.offer(BlockPos.ZERO);

        while (!queue.isEmpty()) {
            BlockPos chunkPos = queue.poll();
            BlockPos offsetChunkPos = chunkPos.offset(origin).atY(chunkPos.getY() + origin.getY());

            if(!isChunkVisible(offsetChunkPos.getX(), offsetChunkPos.getY(), offsetChunkPos.getZ())) {
                continue;
            } else {
                visible.add(offsetChunkPos);
            }

            for (int[] direction : DIRECTIONS) {
                int newX = chunkPos.getX() + direction[0];
                int newY = chunkPos.getY() + direction[1];
                int newZ = chunkPos.getZ() + direction[2];

                if(renderDistance < Mth.abs(newX) ||
                   renderDistance < Mth.abs(newY) ||
                   renderDistance < Mth.abs(newZ))
                    continue;

                BlockPos neighborChunk = new BlockPos(newX, newY, newZ);

                if (!visited.contains(neighborChunk)) {
                    queue.offer(neighborChunk);
                    visited.add(neighborChunk);
                }
            }
        }

        visibleChunks = visible;
    }
}

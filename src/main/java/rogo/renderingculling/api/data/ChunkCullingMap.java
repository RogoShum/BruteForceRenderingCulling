package rogo.renderingculling.api.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class ChunkCullingMap extends CullingMap {
    private int renderDistance = 0;
    private int spacePartitionSize = 0;
    public int queueUpdateCount = 0;
    public int lastQueueUpdateCount = 0;
    private static final int[][] DIRECTIONS = {{0, 1, 0}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
    private boolean updateVisibleChunks = true;
    private Set<BlockPos> visibleChunks = new HashSet<>();

    private int cameraX;
    private int cameraZ;

    @NotNull
    private static BlockPos getOriginPos() {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int cameraX = (int) camera.x >> 4;
        int cameraZ = (int) camera.z >> 4;
        int cameraY = CullingHandler.mapChunkY(camera.y);

        BlockPos origin = new BlockPos(cameraX, cameraY, cameraZ);
        if (origin.getY() < 0) {
            origin = new BlockPos(cameraX, 0, cameraZ);
        } else if (origin.getY() >= CullingHandler.LEVEL_SECTION_RANGE) {
            origin = new BlockPos(cameraX, CullingHandler.LEVEL_SECTION_RANGE - 1, cameraZ);
        }
        return origin;
    }

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

    public void updateCamera() {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        cameraX = (int) camera.x >> 4;
        cameraZ = (int) camera.z >> 4;
    }

    public boolean isChunkOffsetCameraVisible(int x, int y, int z) {
        return isChunkVisible((x >> 4) - cameraX, CullingHandler.mapChunkY(y), (z >> 4) - cameraZ);
    }

    @Override
    public void readData() {
        super.readData();
        updateVisibleChunks = true;
    }

    public boolean isChunkVisible(int posX, int posY, int posZ) {
        int index = 1 + (((posX + renderDistance) * spacePartitionSize * CullingHandler.LEVEL_SECTION_RANGE + (posZ + renderDistance) * CullingHandler.LEVEL_SECTION_RANGE + posY) << 2);
        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > 0;
        }
        return false;
    }

    public void updateVisibleChunks() {
        if (updateVisibleChunks) {
            bfsSearch(getOriginPos());
            updateVisibleChunks = false;
        }
    }

    public Set<BlockPos> getVisibleChunks() {
        return visibleChunks;
    }

    private void bfsSearch(BlockPos origin) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visible = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();

        visited.add(new BlockPos(0, origin.getY(), 0));
        queue.offer(new BlockPos(0, origin.getY(), 0));
        visible.add(new BlockPos(origin.getX(), origin.getY() - CullingHandler.LEVEL_MIN_SECTION_ABS, origin.getZ()));

        while (!queue.isEmpty()) {
            BlockPos chunkPos = queue.poll();
            BlockPos offsetChunkPos = new BlockPos((chunkPos.getX() + origin.getX())
                    , (chunkPos.getY() - CullingHandler.LEVEL_MIN_SECTION_ABS)
                    , (chunkPos.getZ() + origin.getZ()));
            BlockPos absolutePos = new BlockPos(offsetChunkPos.getX() << 4, offsetChunkPos.getY() << 4, offsetChunkPos.getZ() << 4);

            if (!CullingHandler.FRUSTUM.isVisible(new AABB(absolutePos.getX(), absolutePos.getY(), absolutePos.getZ(), absolutePos.getX() + 16, absolutePos.getY() + 16, absolutePos.getZ() + 16))) {
                continue;
            } else {
                boolean isChunkVisible = isChunkVisible(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
                if (CullingHandler.checkCulling)
                    isChunkVisible = !isChunkVisible;

                if (isChunkVisible)
                    visible.add(offsetChunkPos);
                else
                    continue;
            }

            for (int[] direction : DIRECTIONS) {
                int newX = chunkPos.getX() + direction[0];
                int newY = chunkPos.getY() + direction[1];
                int newZ = chunkPos.getZ() + direction[2];

                if (renderDistance < Mth.abs(newX) ||
                        renderDistance < Mth.abs(newZ) ||
                        newY < 0 ||
                        newY >= CullingHandler.LEVEL_SECTION_RANGE)
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

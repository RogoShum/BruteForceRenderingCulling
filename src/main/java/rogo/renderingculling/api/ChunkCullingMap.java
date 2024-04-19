package rogo.renderingculling.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkCullingMap extends CullingMap {
    private int renderDistance = 0;
    private int spacePartitionSize = 0;

    private static final int[][] DIRECTIONS = {{0, 1, 0}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
    private final AtomicReference<Object> uploaderResult = new AtomicReference<>();
    private boolean updateVisibleChunks = true;
    private Set<BlockPos> visibleChunks = new HashSet<>();

    @NotNull
    private static BlockPos getOriginPos() {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int cameraX = (int) camera.x >> 4;
        int cameraZ = (int) camera.z >> 4;
        int cameraY = (int) camera.y;

        if (cameraY < 0)
            cameraY -= 9;

        cameraY = (int) (cameraY * 0.0625) + CullingHandler.LEVEL_MIN_SECTION_ABS;

        BlockPos origin = new BlockPos(cameraX, cameraY, cameraZ);
        if (origin.getY() < 0) {
            origin = new BlockPos(cameraX, 0, cameraZ);
        } else if (origin.getY() >= CullingHandler.LEVEL_HEIGHT_OFFSET) {
            origin = new BlockPos(cameraX, CullingHandler.LEVEL_HEIGHT_OFFSET - 1, cameraZ);
        }
        return origin;
    }

    public boolean isUpdateVisibleChunks() {
        return updateVisibleChunks;
    }

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
        int cameraX = (int) camera.x >> 4;
        int cameraZ = (int) camera.z >> 4;

        if (y < 0)
            y -= 9;

        int chunkX = x >> 4;
        int chunkY = (int) (y * 0.0625) + CullingHandler.LEVEL_MIN_SECTION_ABS;
        int chunkZ = z >> 4;

        int posX = chunkX - cameraX;
        int posZ = chunkZ - cameraZ;

        return isChunkVisible(posX, chunkY, posZ);
    }

    public void setUpdateVisibleChunks(boolean updateVisibleChunks) {
        this.updateVisibleChunks = updateVisibleChunks;
    }

    @Override
    public void readData() {
        super.readData();
        updateVisibleChunks = true;
    }

    public boolean isChunkVisible(int posX, int posY, int posZ) {
        int index = 1 + ((((posX + renderDistance) * spacePartitionSize * CullingHandler.LEVEL_HEIGHT_OFFSET + (posZ + renderDistance) * CullingHandler.LEVEL_HEIGHT_OFFSET + posY) << 2));

        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > 0;
        }

        return false;
    }

    public boolean updateVisibleChunks() {
        if (updateVisibleChunks) {
            bfsSearch(getOriginPos());
            queueUpdateCount++;
            updateVisibleChunks = false;
            return true;
        }

        return false;
    }

    public Set<BlockPos> getVisibleChunks() {
        return visibleChunks;
    }

    public Object getUploadResult() {
        return uploaderResult.get();
    }

    public void setUploaderResult(Object uploaderResult) {
        this.uploaderResult.set(uploaderResult);
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
                if (CullingHandler.INSTANCE.checkCulling)
                    isChunkVisible = !isChunkVisible;

                if (isChunkVisible)
                    visible.add(offsetChunkPos);
            }

            for (int[] direction : DIRECTIONS) {
                int newX = chunkPos.getX() + direction[0];
                int newY = chunkPos.getY() + direction[1];
                int newZ = chunkPos.getZ() + direction[2];

                if (renderDistance < Mth.abs(newX) ||
                        renderDistance < Mth.abs(newZ) ||
                        newY < 0 ||
                        newY >= CullingHandler.LEVEL_HEIGHT_OFFSET)
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

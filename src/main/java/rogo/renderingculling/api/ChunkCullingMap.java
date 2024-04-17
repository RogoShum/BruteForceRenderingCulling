package rogo.renderingculling.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
}

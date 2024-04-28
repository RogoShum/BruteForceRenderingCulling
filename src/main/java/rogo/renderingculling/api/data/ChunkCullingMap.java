package rogo.renderingculling.api.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.*;

public class ChunkCullingMap extends CullingMap {
    private int renderDistance = 0;
    private int spacePartitionSize = 0;
    public int queueUpdateCount = 0;
    public int lastQueueUpdateCount = 0;
    private int cameraX;
    private int cameraZ;

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
        Vec3 camera = CullingHandler.CAMERA.getPosition();
        cameraX = (int) camera.x >> 4;
        cameraZ = (int) camera.z >> 4;
    }

    public boolean isChunkOffsetCameraVisible(int x, int y, int z, boolean checkForChunk) {
        return isChunkVisible((x >> 4) - cameraX, CullingHandler.mapChunkY(y), (z >> 4) - cameraZ, checkForChunk);
    }

    public boolean isChunkVisible(int posX, int posY, int posZ, boolean checkForChunk) {
        int index = 1 + (((posX + renderDistance) * spacePartitionSize * CullingHandler.LEVEL_SECTION_RANGE + (posZ + renderDistance) * CullingHandler.LEVEL_SECTION_RANGE + posY) << 2);
        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > (checkForChunk ? 0 : 127);
        }
        return false;
    }
}

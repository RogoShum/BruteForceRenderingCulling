package rogo.renderingculling.api.data;

import net.minecraft.world.phys.Vec3;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;

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
    protected boolean shouldUpdate() {
        return CullingStateManager.continueUpdateChunk();
    }

    @Override
    int configDelayCount() {
        return Config.getDepthUpdateDelay();
    }

    @Override
    int bindFrameBufferId() {
        return CullingStateManager.CHUNK_CULLING_MAP_TARGET.frameBufferId;
    }

    public void generateIndex(int renderDistance) {
        this.renderDistance = renderDistance;
        spacePartitionSize = 2 * renderDistance + 1;
    }

    public void updateCamera() {
        Vec3 camera = CullingStateManager.CAMERA.getPosition();
        cameraX = (int) camera.x >> 4;
        cameraZ = (int) camera.z >> 4;
    }

    public boolean isChunkOffsetCameraVisible(int x, int y, int z, boolean checkForChunk) {
        return isChunkVisible((x >> 4) - cameraX, CullingStateManager.mapChunkY(y), (z >> 4) - cameraZ, checkForChunk);
    }

    public boolean isChunkVisible(int posX, int posY, int posZ, boolean checkForChunk) {
        int index = 1 + (((posX + renderDistance) * spacePartitionSize * CullingStateManager.LEVEL_SECTION_RANGE + (posZ + renderDistance) * CullingStateManager.LEVEL_SECTION_RANGE + posY) << 2);
        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > (checkForChunk ? 0 : 127);
        }
        return false;
    }
}

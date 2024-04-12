package rogo.renderingculling.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class ChunkCullingMap extends CullingMap {
    private int renderDistance = 0;
    private int spacePartitionSize = 0;

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

    public boolean isChunkVisible(double x, double y, double z) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int cameraX = (int)camera.x >> 4;
        int cameraZ = (int)camera.z >> 4;

        if(y < 0)
            y -= 9;

        int chunkX = (int) x >> 4;
        int chunkY = (int) (y * 0.0625) + CullingHandler.LEVEL_MIN_SECTION_ABS;
        int chunkZ = (int) z >> 4;

        int posX = chunkX - cameraX + renderDistance;
        int posZ = chunkZ - cameraZ + renderDistance;

        int index = 1 + (posX * spacePartitionSize * CullingHandler.LEVEL_HEIGHT_OFFSET + posZ * CullingHandler.LEVEL_HEIGHT_OFFSET + chunkY) << 2;

        if (index > 0 && index < cullingBuffer.limit()) {
            return (cullingBuffer.get(index) & 0xFF) > 0;
        }

        return false;
    }

    public boolean isTransferred() {
        return transferred;
    }

    public void setTransferred(boolean transferred) {
        this.transferred = transferred;
    }
}

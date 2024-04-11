package rogo.renderingculling.api;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import rogo.renderingculling.util.Vec2i;

import java.util.HashMap;

public class ChunkCullingMap extends CullingMap {
    private final HashMap<BlockPos, Integer> screenIndex = new HashMap<>();

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

    public int getPosIndex(BlockPos pos) {
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int spacePartitionSize = 2 * renderDistance + 1;
        int x = pos.getX() + renderDistance;
        int z = pos.getZ() + renderDistance;
        int y = pos.getY();

        return x * spacePartitionSize * CullingHandler.LEVEL_HEIGHT_OFFSET + z * CullingHandler.LEVEL_HEIGHT_OFFSET + y;
    }

    public Vec2i getScreenPosFromIndex(int idx) {
        int y = idx / width;
        int x = idx - (y*width);
        return new Vec2i(x, y);
    }

    public void generateIndex(int renderDistance) {
        screenIndex.clear();
        for(int x = -renderDistance; x <= renderDistance; ++x) {
            for (int z = -renderDistance; z <= renderDistance; ++z) {
                for (int y = 0; y < CullingHandler.LEVEL_HEIGHT_OFFSET; ++y) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Vec2i coord = getScreenPosFromIndex(getPosIndex(pos));
                    if(coord.x() >= 0 && coord.y() >= 0 && coord.x() < this.width && coord.y() < this.height) {
                        screenIndex.put(pos, getPosIndex(pos));
                    }
                }
            }
        }
    }

    public boolean isChunkVisible(double x, double y, double z) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int cameraX = (int)camera.x >> 4;
        int cameraZ = (int)camera.z >> 4;

        if(y < 0)
            y -= 9;

        int chunkX = (int) x >> 4;
        int chunkY = (int) y / 16 + CullingHandler.LEVEL_MIN_SECTION_ABS;
        int chunkZ = (int) z >> 4;
        BlockPos pos = new BlockPos(chunkX - cameraX, chunkY, chunkZ - cameraZ);

        if(screenIndex.containsKey(pos)) {
            Integer index = screenIndex.get(pos);
            float cullingValue = (float) (cullingBuffer.get(1+index*4) & 0xFF) / 255.0f;
            return cullingValue > 0.5;
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

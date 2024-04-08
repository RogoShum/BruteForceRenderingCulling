package rogo.renderingculling.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rogo.renderingculling.util.Vec2i;

import java.util.HashMap;

public class ChunkCullingMap extends CullingMap {
    private final HashMap<BlockPos, Integer> screenIndex = new HashMap<>();

    public ChunkCullingMap(int width, int height) {
        super(width, height);
    }

    @Override
    int delayCount() {
        return Config.UPDATE_DELAY.getValue();
    }

    @Override
    int bindFrameBufferId() {
        return CullingHandler.CHUNK_CULLING_MAP_TARGET.fbo;
    }

    public int getPosIndex(BlockPos pos) {
        int renderDistance = MinecraftClient.getInstance().options.getViewDistance();
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

    public boolean isChunkVisible(BlockPos pos) {
        Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        int cameraX = (int)camera.x >> 4;
        int cameraY = (int)camera.y/16;
        int cameraZ = (int)camera.z >> 4;
        BlockPos cameraPos = new BlockPos(cameraX, cameraY, cameraZ);

        if(pos.getY() < 0)
            pos = new BlockPos(pos.getX(), pos.getY()-9, pos.getZ());

        int chunkX = pos.getX() >> 4;
        int chunkY = pos.getY()/16 + CullingHandler.LEVEL_MIN_SECTION_ABS;
        int chunkZ = pos.getZ() >> 4;
        pos = new BlockPos(chunkX, chunkY, chunkZ).subtract(cameraPos).withY(chunkY);

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

package rogo.renderingculling.util;

import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

public class OcclusionCullerThread extends Thread {
    @Override
    public void run() {
        while (CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.isDone()) {
            if (Config.getCullChunk()) {
                CullingHandler.CHUNK_CULLING_MAP.updateVisibleChunks();
            }
        }
    }
}

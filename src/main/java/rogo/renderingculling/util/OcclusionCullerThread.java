package rogo.renderingculling.util;

import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.DepthCuller;

public class OcclusionCullerThread extends Thread {
    private final DepthCuller<?> depthCuller;

    public OcclusionCullerThread(DepthCuller<?> depthCuller) {
        this.depthCuller = depthCuller;
    }

    @Override
    public void run() {
        while (CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.isDone()) {
            if (Config.getCullChunk()) {
                CullingHandler.CHUNK_CULLING_MAP.updateVisibleChunks();
            }
        }
    }

    public DepthCuller<?> getDepthCuller() {
        return this.depthCuller;
    }
}

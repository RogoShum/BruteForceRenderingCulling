package rogo.renderingculling.util;

import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.VisibleChunkUploader;

import static rogo.renderingculling.api.CullingHandler.hasMod;

public class OcclusionCullerThread extends Thread {
    private final VisibleChunkUploader<?> uploader;

    public OcclusionCullerThread() {
        this.uploader = getUploader();
    }

    @Override
    public void run() {
        while (CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.isDone()) {
            if (Config.getCullChunk()) {
                if(CullingHandler.CHUNK_CULLING_MAP.updateVisibleChunks()) {

                }
                uploader.update();
            }
        }
    }

    private VisibleChunkUploader<?> getUploader() {
        VisibleChunkUploader<?> uploader1;

        if (hasMod("embeddium") || hasMod("rubidium")) {
            try {
                uploader1 = Class.forName("rogo.renderingculling.util.SodiumChunkUploader").asSubclass(VisibleChunkUploader.class).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            uploader1 = new VanillaChunkUploader();
        }

        return uploader1;
    }
}

package rogo.renderingculling.util;

import net.minecraft.client.Minecraft;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.ModLoader;

public class OcclusionCullerThread extends Thread {
    public static OcclusionCullerThread INSTANCE;
    private boolean finished = false;

    public OcclusionCullerThread() {
        if (INSTANCE != null) {
            INSTANCE.finished = true;
        }
        INSTANCE = this;
    }

    @Override
    public void run() {
        while (!finished) {
            try {
                if (CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.isDone()) {
                    if (Config.getAsyncChunkRebuild()) {
                        if (ModLoader.hasSodium()) {
                            SodiumSectionAsyncUtil.asyncSearchRebuildSection();
                        } else {
                            //VanillaAsyncUtil.asyncSearchRebuildSection();
                        }
                    }
                }

                if (Minecraft.getInstance().level == null) {
                    finished = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void shouldUpdate() {
        if (ModLoader.hasSodium() && Config.getAsyncChunkRebuild()) {
            SodiumSectionAsyncUtil.shouldUpdate();
        }
    }
}

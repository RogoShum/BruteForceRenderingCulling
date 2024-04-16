package rogo.renderingculling.api;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

public interface IDualityChunkRenderList {
    void unobserved(RenderSection render);
    void original(int frame);
    int getLastWaveFrame();
    void observer(int frame);
}

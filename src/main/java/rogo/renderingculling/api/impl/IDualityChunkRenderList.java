package rogo.renderingculling.api.impl;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;

public interface IDualityChunkRenderList {
    void copyRenderData(int frame);
    void unObserved(RenderSection render);
    void observer();
}

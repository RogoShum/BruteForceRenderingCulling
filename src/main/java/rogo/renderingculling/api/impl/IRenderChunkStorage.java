package rogo.renderingculling.api.impl;

import net.minecraft.client.renderer.LevelRenderer;

public interface IRenderChunkStorage {
    void copy(LevelRenderer.RenderChunkStorage storage);
}

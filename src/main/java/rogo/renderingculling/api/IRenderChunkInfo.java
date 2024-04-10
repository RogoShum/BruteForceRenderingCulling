package rogo.renderingculling.api;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface IRenderChunkInfo {
    ChunkRenderDispatcher.RenderChunk getRenderChunk();
}

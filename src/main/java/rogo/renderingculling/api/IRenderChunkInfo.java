package rogo.renderingculling.api;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface IRenderChunkInfo {
    ChunkRenderDispatcher.RenderChunk getRenderChunk();
}

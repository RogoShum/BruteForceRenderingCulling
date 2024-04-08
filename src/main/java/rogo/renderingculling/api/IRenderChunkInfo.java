package rogo.renderingculling.api;

import net.minecraft.client.render.chunk.ChunkBuilder;

public interface IRenderChunkInfo {
    ChunkBuilder.BuiltChunk getRenderChunk();
}

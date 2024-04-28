package rogo.renderingculling.api.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IEntitiesForRender {
    ObjectArrayList<?> renderChunksInFrustum();
    ChunkRenderDispatcher.RenderChunk invokeGetRelativeFrom(BlockPos pos, ChunkRenderDispatcher.RenderChunk chunk, Direction dir);
    ChunkRenderDispatcher.RenderChunk invokeGetRenderChunkAt(BlockPos pos);

}

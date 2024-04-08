package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rogo.renderingculling.api.IRenderChunkInfo;

@Mixin(targets = "net.minecraft.client.renderer.LevelRenderer$RenderChunkInfo")
public class MixinRenderChunkInfo implements IRenderChunkInfo {

    @Final
    @Shadow
    ChunkRenderDispatcher.RenderChunk chunk;

    @Override
    public ChunkRenderDispatcher.RenderChunk getRenderChunk() {
        return chunk;
    }
}

package rogo.renderingculling.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rogo.renderingculling.api.IRenderChunkInfo;

@Mixin(targets = "net.minecraft.client.render.WorldRenderer$ChunkInfo")
public class MixinRenderChunkInfo implements IRenderChunkInfo {

    @Final
    @Shadow
    ChunkBuilder.BuiltChunk chunk;

    @Override
    public ChunkBuilder.BuiltChunk getRenderChunk() {
        return chunk;
    }
}

package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rogo.renderingculling.api.IChunkRenderList;

@Mixin(ChunkRenderList.class)
public abstract class MixinChunkRenderList implements IChunkRenderList {
    @Shadow(remap = false)
    private int lastVisibleFrame;

    @Override
    public void setLastVisibleFrame(int frame) {
        this.lastVisibleFrame = frame;
    }
}

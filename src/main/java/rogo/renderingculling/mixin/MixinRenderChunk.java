package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rogo.renderingculling.api.IRenderSectionVisibility;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public abstract class MixinRenderChunk implements IRenderSectionVisibility {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos origin;
    @Unique
    private int cullingLastVisibleFrame;

    private volatile boolean asyncSearched = false;
    private volatile boolean asyncSubmitted = false;

    @Override
    public boolean shouldCheckVisibility(int frame) {
        return frame != cullingLastVisibleFrame;
    }

    @Override
    public void updateVisibleTick(int frame) {
        cullingLastVisibleFrame = frame;
    }

    @Override
    public int getPositionX() {
        return origin.getX();
    }

    @Override
    public int getPositionY() {
        return origin.getY();
    }

    @Override
    public int getPositionZ() {
        return origin.getZ();
    }

    public boolean isSubmittedRebuild() {
        return asyncSubmitted;
    }

    public void setSubmittedRebuild(boolean submited) {
        asyncSubmitted = submited;
    }

    public boolean isSearched() {
        return asyncSearched;
    }

    public void setSearch(boolean search) {
        asyncSearched = search;
    }
}

package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rogo.renderingculling.api.IRenderSectionVisibility;

@Mixin(RenderSection.class)
public abstract class MixinRenderSection implements IRenderSectionVisibility {

    @Shadow(remap = false)
    public abstract int getOriginX();

    @Shadow(remap = false)
    public abstract int getOriginY();

    @Shadow(remap = false)
    public abstract int getOriginZ();

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
        return getOriginX();
    }

    @Override
    public int getPositionY() {
        return getOriginY();
    }

    @Override
    public int getPositionZ() {
        return getOriginZ();
    }
}

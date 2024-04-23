package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;

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

package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rogo.renderingculling.api.IRenderSectionVisibility;

@Mixin(RenderSection.class)
public abstract class MixinRenderSection implements IRenderSectionVisibility {

    @Shadow(remap = false) public abstract int getOriginX();

    @Shadow(remap = false) public abstract int getOriginY();

    @Shadow(remap = false) public abstract int getOriginZ();

    @Unique
    private int cullingLastVisibleTick;

    @Override
    public boolean shouldCheckVisibility(int clientTick) {
        return clientTick - cullingLastVisibleTick > 20;
    }

    @Override
    public void updateVisibleTick(int clientTick) {
        cullingLastVisibleTick = clientTick;
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

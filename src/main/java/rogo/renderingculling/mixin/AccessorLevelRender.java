package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface AccessorLevelRender {
    @Accessor("cullingFrustum")
    Frustum getCullingFrustum();

    @Accessor("capturedFrustum")
    Frustum getCapturedFrustum();
}


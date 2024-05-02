package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.util.DummySection;

@Mixin(Frustum.class)
public abstract class MixinFrustum {

    @Inject(method = "isVisible", at = @At(value = "RETURN"), cancellable = true)
    public void afterVisible(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if (CullingStateManager.applyFrustum && Config.shouldCullChunk() && cir.getReturnValue() && !CullingStateManager.shouldRenderChunk(new DummySection(aabb), true))
            cir.setReturnValue(false);
    }
}

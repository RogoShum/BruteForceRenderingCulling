package rogo.renderingculling.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.CullingHandler;

@Mixin(Frustum.class)
public class MixinFrustum {
    @Inject(method = "isVisible", at = @At(value = "HEAD"), cancellable = true)
    public void onApplyFrustum(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if(CullingHandler.INSTANCE.applyFrustum) {
            Vec3 center = aabb.getCenter();
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            if(center.subtract(camera.getPosition()).normalize().dot(new Vec3(camera.getLookVector())) < 0 && center.distanceToSqr(camera.getPosition()) > 512)
                cir.setReturnValue(false);
            else
                cir.setReturnValue(CullingHandler.INSTANCE.shouldRenderChunk(aabb));
        }
    }
}
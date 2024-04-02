package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IRenderChunkInfo;

@Mixin(Frustum.class)
public class MixinFrustum {

    @Inject(method = "isVisible", at = @At(value = "HEAD"), cancellable = true)
    public void onApplyFrustum(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        if(Config.CULL_CHUNK.get() && CullingHandler.applyFrustum && CullingHandler.SHADER_LOADER != null) {
            cir.setReturnValue(CullingHandler.INSTANCE.shouldRenderChunk(aabb));
        }
    }
}

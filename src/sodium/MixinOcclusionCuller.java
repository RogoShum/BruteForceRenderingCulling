package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.util.OcclusionCullerThread;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

@Mixin(OcclusionCuller.class)
public abstract class MixinOcclusionCuller {

    @Inject(method = "isSectionVisible", at = @At(value = "RETURN"), remap = false, cancellable = true)
    private static void onIsSectionVisible(RenderSection section, Viewport viewport, float maxDistance, CallbackInfoReturnable<Boolean> cir) {
        if (Config.shouldCullChunk() && cir.getReturnValue() && !CullingHandler.shouldRenderChunk((IRenderSectionVisibility) section, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "findVisible", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void onFindVisible(OcclusionCuller.Visitor visitor, Viewport viewport, float searchDistance, boolean useOcclusionCulling, int frame, CallbackInfo ci) {
        if (SodiumSectionAsyncUtil.asyncSearching && Thread.currentThread() != OcclusionCullerThread.INSTANCE) {
            SodiumSectionAsyncUtil.asyncSearching = false;
            ci.cancel();
        }
    }
}
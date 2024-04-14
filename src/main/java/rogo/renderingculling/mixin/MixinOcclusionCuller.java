package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IRenderSectionVisibility;

@Mixin(OcclusionCuller.class)
public abstract class MixinOcclusionCuller {
    @Inject(method = "isSectionVisible", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void onIsSectionVisible(RenderSection section, Viewport viewport, float maxDistance, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CullingHandler.INSTANCE.shouldRenderChunk((IRenderSectionVisibility) section, true));
    }
}
package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {
    @Inject(method = "isWithinRenderDistance", at = @At(value = "RETURN"), remap = false, cancellable = true)
    public void onIsWithinRenderDistance(RenderSection section, CallbackInfoReturnable<Boolean> cir) {
        if (Config.shouldCullChunk() && cir.getReturnValue() && !CullingHandler.shouldRenderChunk((IRenderSectionVisibility) section, true))
            cir.setReturnValue(false);
    }
}
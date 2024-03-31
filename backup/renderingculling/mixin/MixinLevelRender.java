package rogo.renderingculling.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.Config;
import rogo.renderingculling.CullingHandler;

@Mixin(LevelRenderer.class)
public class MixinLevelRender {
    @Inject(method = "setupRender", at = @At(value = "HEAD"), cancellable = true)
    public void onSetupRender(Camera camera, Frustum p_194340_, boolean p_194341_, boolean p_194342_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE.shouldSkipSetupRender(camera))
            ci.cancel();
    }
}

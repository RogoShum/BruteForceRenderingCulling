package rogo.renderingculling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingStateManager;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    protected abstract double getFov(Camera p_109142_, float p_109143_, boolean p_109144_);

    @Shadow
    public abstract Camera getMainCamera();

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    public void afterRunTick(float p_109090_, long p_109091_, PoseStack p_109092_, CallbackInfo ci) {
        CullingStateManager.FOV = this.getFov(this.getMainCamera(), p_109090_, true);
    }
}

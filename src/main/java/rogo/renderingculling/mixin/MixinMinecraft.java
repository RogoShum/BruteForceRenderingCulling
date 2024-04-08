package rogo.renderingculling.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {
    @Inject(method = "render", at = @At(value = "RETURN"))
    public void onPopPush(boolean p_91384_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE != null)
            CullingHandler.INSTANCE.onProfilerPopPush("afterRunTick");
    }
}

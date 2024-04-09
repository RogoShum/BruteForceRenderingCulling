package rogo.renderingculling.mixin;

import net.minecraft.util.profiling.InactiveProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;

@Mixin(InactiveProfiler.class)
public class MixinInactiveProfiler {

    @Inject(method = "popPush(Ljava/lang/String;)V", at = @At(value = "HEAD"))
    public void onPopPush(String p_18395_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE != null)
            CullingHandler.INSTANCE.onProfilerPopPush(p_18395_);
    }

    @Inject(method = "push(Ljava/lang/String;)V", at = @At(value = "HEAD"))
    public void onPush(String p_18395_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE != null)
            CullingHandler.INSTANCE.onProfilerPush(p_18395_);
    }
}

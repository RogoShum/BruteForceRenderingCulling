package rogo.renderingculling.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", shift = At.Shift.AFTER))
    public void afterRunTick(boolean p_91384_, CallbackInfo ci) {
        CullingHandler.onProfilerPopPush("afterRunTick");
    }

    @Inject(method = "runTick", at = @At(value = "HEAD"))
    public void beforeRunTick(boolean p_91384_, CallbackInfo ci) {
        CullingHandler.onProfilerPopPush("beforeRunTick");
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    public void onJoinWorld(ClientLevel world, CallbackInfo ci) {
        CullingHandler.onWorldUnload(world);
    }
}

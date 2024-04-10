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
    @Inject(method = "runTick", at = @At(value = "RETURN"))
    public void onPopPush(boolean p_91384_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE != null)
            CullingHandler.INSTANCE.onProfilerPopPush("afterRunTick");
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    public void onJoinWorld(ClientLevel world, CallbackInfo ci) {
        CullingHandler.INSTANCE.onWorldUnload(world);
    }
}

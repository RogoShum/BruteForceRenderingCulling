package rogo.renderingculling.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.event.WorldUnloadEvent;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {
    @Inject(method = "render", at = @At(value = "RETURN"))
    public void onRender(boolean p_91384_, CallbackInfo ci) {
        if(CullingHandler.INSTANCE != null)
            CullingHandler.INSTANCE.onProfilerPopPush("afterRunTick");
    }

    @Inject(method = "joinWorld", at = @At(value = "HEAD"))
    public void onJoinWorld(ClientWorld world, CallbackInfo ci) {
        WorldUnloadEvent.WORLD_UNLOAD.invoker().onWorldUnload(world);
    }
}

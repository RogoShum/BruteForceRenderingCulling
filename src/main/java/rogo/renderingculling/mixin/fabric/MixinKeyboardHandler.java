package rogo.renderingculling.mixin.fabric;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.CullingHandler;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Inject(method = "keyPress", at= @At(value = "RETURN"))
    <E extends Entity> void onKeyPress(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (l == Minecraft.getInstance().getWindow().getWindow()) {
            CullingHandler.onProfilerPush("onKeyboardInput");
        }
    }
}

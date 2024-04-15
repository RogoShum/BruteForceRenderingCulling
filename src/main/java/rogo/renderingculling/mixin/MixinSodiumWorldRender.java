package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.util.SodiumChunkUploader;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRender {

    @Inject(method = "setupTerrain", at = @At(value = "HEAD"), remap = false)
    public void onSetup(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {

    }
}

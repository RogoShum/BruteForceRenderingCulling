package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

import java.util.List;

@Mixin(RegionChunkRenderer.class)
public abstract class MixinRegionChunkRenderer {

    @Inject(method = "buildDrawBatches", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void onBuildDrawBatches(List<RenderSection> sections, BlockRenderPass pass, ChunkCameraContext camera, CallbackInfoReturnable<Boolean> cir) {
        if (Config.getAsyncChunkRebuild() && !sections.isEmpty() && SodiumSectionAsyncUtil.shouldCancelBuild(sections.stream().findFirst().get())) {
            cir.setReturnValue(false);
        }
    }
}
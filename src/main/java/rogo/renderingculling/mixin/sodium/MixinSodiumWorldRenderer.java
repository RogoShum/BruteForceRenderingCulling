package rogo.renderingculling.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public abstract class MixinSodiumWorldRenderer {

    @Shadow(remap = false)
    private RenderSectionManager renderSectionManager;

    @Inject(method = "setupTerrain", at = @At(value = "HEAD"), remap = false)
    public void injectTerrainSetup(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        if (Config.shouldCullChunk()) {
            SodiumSectionAsyncUtil.update(viewport, ((AccessorRenderSectionManager) this.renderSectionManager).invokeSearchDistance()
                    , ((AccessorRenderSectionManager) this.renderSectionManager).invokeShouldUseOcclusionCulling(camera, spectator));
            if(SodiumSectionAsyncUtil.needSync) {
                this.renderSectionManager.markGraphDirty();
                SodiumSectionAsyncUtil.needSync = false;
            }
        }
    }

    @Inject(method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V", at = @At(value = "HEAD"), remap = false)
    public void beforeRenderBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
        SodiumSectionAsyncUtil.renderingEntities = true;
    }

    @Inject(method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V", at = @At(value = "RETURN"), remap = false)
    public void afterRenderBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
        SodiumSectionAsyncUtil.renderingEntities = false;
    }
}
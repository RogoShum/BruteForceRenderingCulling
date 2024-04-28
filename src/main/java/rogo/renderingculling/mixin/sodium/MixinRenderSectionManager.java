package rogo.renderingculling.mixin.sodium;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @Shadow(remap = false)
    @Final
    private Long2ReferenceMap<RenderSection> sectionByPosition;

    @Shadow(remap = false) private @NotNull SortedRenderLists renderLists;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ClientLevel world, int renderDistance, CommandList commandList, CallbackInfo ci) {
        SodiumSectionAsyncUtil.fromSectionManager(this.sectionByPosition, world);
    }

    @Inject(method = "isSectionVisible", at = @At(value = "RETURN"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir, RenderSection section) {
        if (Config.shouldCullChunk())
            cir.setReturnValue(CullingHandler.shouldRenderChunk((IRenderSectionVisibility) section, false));
    }

    @ModifyVariable(name = "visitor", method = "createTerrainRenderList", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;findVisible(Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller$Visitor;Lme/jellysquid/mods/sodium/client/render/viewport/Viewport;FZI)V", shift = At.Shift.BEFORE), remap = false)
    private VisibleChunkCollector onCreateTerrainRenderList(VisibleChunkCollector value) {
        if (Config.getAsyncChunkRebuild()) {
            VisibleChunkCollector collector = CullingHandler.renderingIris() ? SodiumSectionAsyncUtil.getShadowCollector() : SodiumSectionAsyncUtil.getChunkCollector();
            return collector == null ? value : collector;
        }
        return value;
    }

    @Inject(method = "updateChunks", at = @At(value = "HEAD"), remap = false)
    private void onCreateTerrainRenderList(boolean updateImmediately, CallbackInfo ci) {
        if (Config.getAsyncChunkRebuild()) {
            VisibleChunkCollector collector = CullingHandler.renderingIris() ? SodiumSectionAsyncUtil.getShadowCollector() : SodiumSectionAsyncUtil.getChunkCollector();
            if(collector != null)
                this.renderLists = collector.createRenderLists();
        }
    }
}
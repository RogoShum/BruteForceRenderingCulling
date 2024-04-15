package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IRenderSectionVisibility;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @Shadow protected abstract RenderSection getRenderSection(int x, int y, int z);

    @Shadow private @NotNull SortedRenderLists renderLists;

    @Shadow private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists;

    @Inject(method = "isSectionVisible", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;getLastVisibleFrame()I"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir, RenderSection section) {
        cir.setReturnValue(CullingHandler.INSTANCE.shouldRenderChunk((IRenderSectionVisibility) section, false));
    }

    @Inject(method = "createTerrainRenderList", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;findVisible(Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller$Visitor;Lme/jellysquid/mods/sodium/client/render/viewport/Viewport;FZI)V"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onCreateTerrainRenderList(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci, float searchDistance, boolean useOcclusionCulling, VisibleChunkCollector visitor) {
        if(Config.getCullChunk() && CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.isDone()) {
            ci.cancel();
            Queue<BlockPos> visibleChunks = CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks();
            visibleChunks.forEach(chunkPos -> {
                RenderSection section = this.getRenderSection(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
                if(section != null) {
                    visitor.visit(section, true);
                }
            });
            this.renderLists = visitor.createRenderLists();
            this.rebuildLists = visitor.getRebuildLists();
        }
    }
}
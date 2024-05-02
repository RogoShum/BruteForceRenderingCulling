package rogo.renderingculling.mixin.sodium;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.*;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionManager;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

import java.util.Map;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @Shadow(remap = false)
    @Final
    private Long2ReferenceMap<RenderSection> sections;

    @Shadow(remap = false)
    @Final
    private ChunkTracker tracker;

    @Mutable
    @Shadow(remap = false)
    @Final
    private ChunkRenderList chunkRenderList;

    @Mutable
    @Shadow(remap = false)
    @Final
    private ObjectList<BlockEntity> visibleBlockEntities;

    @Mutable
    @Shadow(remap = false)
    @Final
    private Map<ChunkUpdateType, PriorityQueue<RenderSection>> rebuildQueues;

    @Mutable
    @Shadow(remap = false)
    @Final
    private ObjectList<RenderSection> tickableChunks;

    @Shadow(remap = false)
    private boolean needsUpdate;

    @Shadow(remap = false)
    protected abstract void initSearch(Camera camera, Frustum frustum, int frame, boolean spectator);

    @Shadow(remap = false)
    @Final
    private RenderRegionManager regions;

    @Shadow(remap = false)
    protected abstract void setup(Camera camera);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(SodiumWorldRenderer worldRenderer, BlockRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CommandList commandList, CallbackInfo ci) {
        SodiumSectionAsyncUtil.fromSectionManager(this.sections, tracker, renderDistance);
    }

    @Inject(method = "isSectionVisible", at = @At(value = "RETURN"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir, RenderSection section) {
        if (Config.shouldCullChunk())
            cir.setReturnValue(CullingStateManager.shouldRenderChunk((IRenderSectionVisibility) section, false));
    }

    @Inject(method = "isWithinRenderDistance", at = @At(value = "RETURN"), remap = false, cancellable = true)
    public void onIsWithinRenderDistance(RenderSection section, CallbackInfoReturnable<Boolean> cir) {
        if (Config.shouldCullChunk() && cir.getReturnValue() && !CullingStateManager.shouldRenderChunk((IRenderSectionVisibility) section, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "update", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void onUpdate(Camera camera, Frustum frustum, int frame, boolean spectator, CallbackInfo ci) {
        CullingStateManager.updating();
        if (Config.getAsyncChunkRebuild()) {
            this.regions.updateVisibility(frustum);
            this.setup(camera);
            this.initSearch(camera, frustum, frame, spectator);
            SyncChunk(true);
            this.needsUpdate = false;
            ci.cancel();
        }
    }

    @Inject(method = "updateChunks", at = @At(value = "HEAD"), remap = false)
    private void onUpdateChunks(CallbackInfo ci) {
        if (Config.getAsyncChunkRebuild()) {
            SyncChunk(false);
        }
    }

    public void SyncChunk(boolean rebuild) {
        SodiumSectionAsyncUtil.AsynchronousChunkCollector collector = CullingStateManager.renderingIris() ? SodiumSectionAsyncUtil.getShadowCollector() : SodiumSectionAsyncUtil.getChunkCollector();
        if (collector != null) {
            this.chunkRenderList = collector.getChunkRenderList();
            this.visibleBlockEntities = collector.getVisibleBlockEntities();
            this.tickableChunks = collector.getTickableChunks();
            if (rebuild) {
                this.rebuildQueues = collector.getRebuildLists();
            }
        }
    }
}
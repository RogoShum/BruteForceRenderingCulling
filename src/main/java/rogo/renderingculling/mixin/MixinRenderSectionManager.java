package rogo.renderingculling.mixin;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
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
import rogo.renderingculling.util.SodiumAsyncSectionUtil;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @Shadow(remap = false)
    protected abstract RenderSection getRenderSection(int x, int y, int z);

    @Shadow(remap = false)
    private @NotNull SortedRenderLists renderLists;

    @Shadow(remap = false)
    private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists;

    @Shadow(remap = false)
    @Final
    private Long2ReferenceMap<RenderSection> sectionByPosition;

    @Shadow(remap = false) @Final private OcclusionCuller occlusionCuller;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ClientLevel world, int renderDistance, CommandList commandList, CallbackInfo ci) {
        SodiumAsyncSectionUtil.fromSectionManager(this.sectionByPosition, world);
    }

    @Inject(method = "isSectionVisible", at = @At(value = "RETURN"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onIsSectionVisible(int x, int y, int z, CallbackInfoReturnable<Boolean> cir, RenderSection section) {
        if (Config.shouldCullChunk())
            cir.setReturnValue(CullingHandler.INSTANCE.shouldRenderChunk((IRenderSectionVisibility) section, false));
    }

    public RenderSection getSectionFromObject(Object pos) {
        return getSectionFromPos((BlockPos) pos);
    }

    public RenderSection getSectionFromPos(BlockPos pos) {
        return this.getRenderSection(pos.getX(), pos.getY(), pos.getZ());
    }

    @Inject(method = "createTerrainRenderList", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;findVisible(Lme/jellysquid/mods/sodium/client/render/chunk/occlusion/OcclusionCuller$Visitor;Lme/jellysquid/mods/sodium/client/render/viewport/Viewport;FZI)V"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onCreateTerrainRenderList(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci, float searchDistance, boolean useOcclusionCulling, VisibleChunkCollector visitor) {
        if (Config.shouldCullChunk()) {
            ci.cancel();
            SodiumAsyncSectionUtil.ChunkContext chunkContext = SodiumAsyncSectionUtil.handleRenderSearch(frame, viewport, this.occlusionCuller, visitor, this::getSectionFromObject);
            renderLists = chunkContext.renderLists();
            rebuildLists = chunkContext.rebuildLists();
        }
    }
}
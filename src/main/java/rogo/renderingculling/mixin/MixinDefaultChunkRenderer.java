package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.gl.device.MultiDrawBatch;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.IRenderSectionVisibility;
import rogo.renderingculling.util.SodiumAsyncSectionUtil;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinDefaultChunkRenderer {

    @Inject(method = "fillCommandBuffer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/SectionRenderDataStorage;getDataPointer(I)J"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void onGetBlockShape(MultiDrawBatch batch, RenderRegion renderRegion, SectionRenderDataStorage renderDataStorage, ChunkRenderList renderList, CameraTransform camera, TerrainRenderPass pass, boolean useBlockFaceCulling, CallbackInfo ci, ByteIterator iterator, int originX, int originY, int originZ, int sectionIndex, int chunkX, int chunkY, int chunkZ) {
        if(Config.shouldCullChunk()) {
            RenderSection section = SodiumAsyncSectionUtil.getSectionFromPos(new BlockPos(chunkX, chunkY, chunkZ));
            if (section != null) {
                section.setLastVisibleFrame(SodiumAsyncSectionUtil.getSodiumLastChunkUpdateFrame());
            }
        }
    }
}
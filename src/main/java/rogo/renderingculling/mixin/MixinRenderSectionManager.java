package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphInfo;
import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphIterationQueue;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.IChunkGraphInfo;
import rogo.renderingculling.api.CullingHandler;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @Inject(method = "iterateChunks", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/graph/ChunkGraphIterationQueue;getDirection(I)Lnet/minecraft/core/Direction;"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    public void onIterateChunks(Camera camera, Frustum frustum, int frame, boolean spectator, CallbackInfo ci, ChunkGraphIterationQueue queue, int i, RenderSection section) {
        if(!section.isEmpty()) {
            ((IChunkGraphInfo)section.getGraphInfo()).setVisible(CullingHandler.INSTANCE.shouldRenderChunk(
                    new AABB(section.getOriginX(), (double)section.getOriginY(), (double)section.getOriginZ(), (section.getOriginX() + 16), (double)(section.getOriginY() + 16), (double)(section.getOriginZ() + 16))));
        } else
            ((IChunkGraphInfo)section.getGraphInfo()).setVisible(true);

    }

    @Inject(method = "isCulled", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void onIsCulled(ChunkGraphInfo node, Direction from, Direction _to, CallbackInfoReturnable<Boolean> cir) {
        if(!((IChunkGraphInfo)node).isVisible())
            cir.setReturnValue(true);
    }
}
package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphInfo;
import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphIterationQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.AccessorChunkGraphInfo;
import rogo.renderingculling.Config;
import rogo.renderingculling.CullingHandler;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {
    @Redirect(method = "iterateChunks", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/graph/ChunkGraphIterationQueue;getRender(I)Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;", opcode = Opcodes.GETFIELD), remap = false)
    public RenderSection onIterateChunks(ChunkGraphIterationQueue queue, int i) {
        RenderSection instance = queue.getRender(i);
        if(!instance.isEmpty()) {
            ((AccessorChunkGraphInfo)instance.getGraphInfo()).setVisible(CullingHandler.INSTANCE.shouldRenderChunk(
                    new AABB(instance.getOriginX(), (double)instance.getOriginY(), (double)instance.getOriginZ(), (instance.getOriginX() + 16), (double)(instance.getOriginY() + 16), (double)(instance.getOriginZ() + 16))
                    , new BlockPos(instance.getOriginX(), instance.getOriginY(), instance.getOriginZ())));
        } else
            ((AccessorChunkGraphInfo)instance.getGraphInfo()).setVisible(true);
        return instance;
    }

    @Inject(method = "isCulled", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void onIsCulled(ChunkGraphInfo node, Direction from, Direction _to, CallbackInfoReturnable<Boolean> cir) {
        if(!((AccessorChunkGraphInfo)node).isVisible())
            cir.setReturnValue(true);
    }
}

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
import rogo.renderingculling.api.CullingHandler;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {
    @Inject(method = "addVisible", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void onBfsEnqueue(RenderSection section, Direction flow, CallbackInfo ci) {
        if(!section.isEmpty() && !CullingHandler.INSTANCE.shouldRenderChunk(
                new AABB(section.getOriginX(), section.getOriginY(), section.getOriginZ(), (section.getOriginX() + 16), (section.getOriginY() + 16), (section.getOriginZ() + 16))))
            ci.cancel();
    }
}
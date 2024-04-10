package rogo.renderingculling.mixin;

/*
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.CullingHandler;

@Mixin(OcclusionCuller.class)
public abstract class MixinRenderSectionManager {
    @Inject(method = "isSectionVisible", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void onIsSectionVisible(RenderSection section, Viewport viewport, float maxDistance, CallbackInfoReturnable<Boolean> cir) {
        if(!CullingHandler.INSTANCE.shouldRenderChunk(
                new AABB(section.getOriginX(), section.getOriginY(), section.getOriginZ(), (section.getOriginX() + 16), (section.getOriginY() + 16), (section.getOriginZ() + 16))))
            cir.setReturnValue(false);
    }
}
 */
package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.api.CullingHandler;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRender {
    @Inject(method = "shouldRender", at=@At("RETURN"), cancellable = true)
    public <E extends Entity> void onShouldRender(E p_114398_, Frustum p_114399_, double p_114400_, double p_114401_, double p_114402_, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue() && !p_114398_.noCulling && CullingHandler.INSTANCE.shouldSkipEntity(p_114398_)) {
            cir.setReturnValue(false);
        }
    }
}

package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

@Mixin(RenderRegion.class)
public abstract class MixinRenderRegion {
    @Shadow(remap = false)
    @Final
    private RenderSection[] sections;

    @Inject(method = "getSection", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetSection(int id, CallbackInfoReturnable<RenderSection> cir) {
        if (SodiumSectionAsyncUtil.renderingEntities && this.sections[id] == null)
            cir.setReturnValue(new RenderSection((RenderRegion) (Object) this, 0, 0, 0));
    }
}

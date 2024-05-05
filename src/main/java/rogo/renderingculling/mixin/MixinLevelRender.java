package rogo.renderingculling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.impl.IEntitiesForRender;
import rogo.renderingculling.util.VanillaAsyncUtil;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRender implements IEntitiesForRender {

    @Final
    @Shadow
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Shadow
    @Nullable
    private ViewArea viewArea;

    @Inject(method = "setupRender", at = @At(value = "HEAD"))
    public void onSetupRenderHead(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_, CallbackInfo ci) {
        if (this.viewArea != null) {
            VanillaAsyncUtil.update((LevelRenderer) (Object) this, this.viewArea.sections.length);
        }
    }

    @Inject(method = "applyFrustum", at = @At(value = "HEAD"))
    public void onApplyFrustumHead(Frustum p_194355_, CallbackInfo ci) {
        CullingStateManager.applyFrustum = true;
        CullingStateManager.updating();
    }

    @Inject(method = "applyFrustum", at = @At(value = "RETURN"))
    public void onApplyFrustumReturn(Frustum p_194355_, CallbackInfo ci) {
        CullingStateManager.applyFrustum = false;
    }

    @Inject(method = "prepareCullFrustum", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;<init>(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    public void onPrepareCullFrustum(PoseStack p_172962_, Vec3 p_172963_, Matrix4f p_172964_, CallbackInfo ci) {
        CullingStateManager.PROJECTION_MATRIX = new Matrix4f(p_172964_);
    }

    @Override
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> renderChunksInFrustum() {
        return visibleSections;
    }
}


package rogo.renderingculling.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingRenderEvent;
import rogo.renderingculling.api.ICullingShader;

import javax.annotation.Nullable;

@Mixin(ShaderInstance.class)
public abstract class MixinShaderInstance implements ICullingShader {
    @Shadow
    @Nullable
    public Uniform getUniform(String p_173349_) {
        return null;
    }

    @Nullable
    public Uniform CULLING_CAMERA_POS;
    @Nullable
    public Uniform RENDER_DISTANCE;
    @Nullable
    public Uniform DEPTH_SIZE;
    @Nullable
    public Uniform CULLING_SIZE;
    @Nullable
    public Uniform ENTITY_CULLING_SIZE;
    @Nullable
    public Uniform DEPTH_OFFSET;
    @Nullable
    public Uniform CULLING_FRUSTUM;
    @Nullable
    public Uniform FRUSTUM_POS;
    @Nullable
    public Uniform CULLING_VIEW_MAT;
    @Nullable
    public Uniform CULLING_PROJ_MAT;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V")
    public void construct(CallbackInfo ci) {
        this.CULLING_CAMERA_POS = this.getUniform("CullingCameraPos");
        this.RENDER_DISTANCE = this.getUniform("RenderDistance");
        this.DEPTH_SIZE = this.getUniform("DepthSize");
        this.CULLING_SIZE = this.getUniform("CullingSize");
        this.DEPTH_OFFSET = this.getUniform("DepthOffset");
        this.ENTITY_CULLING_SIZE = this.getUniform("EntityCullingSize");
        this.CULLING_FRUSTUM = this.getUniform("CullingFrustum");
        this.FRUSTUM_POS = this.getUniform("FrustumPos");
        this.CULLING_VIEW_MAT = this.getUniform("CullingViewMat");
        this.CULLING_PROJ_MAT = this.getUniform("CullingProjMat");
    }

    @Override
    public Uniform getCullingFrustum() {return CULLING_FRUSTUM;}
    @Override
    public Uniform getCullingCameraPos() {
        return CULLING_CAMERA_POS;
    }

    @Override
    public Uniform getRenderDistance() {
        return RENDER_DISTANCE;
    }

    @Override
    public Uniform getDepthSize() {
        return DEPTH_SIZE;
    }
    @Override
    public Uniform getCullingSize() {
        return CULLING_SIZE;
    }
    @Override
    public Uniform getDepthOffset() {
        return DEPTH_OFFSET;
    }
    @Override
    public Uniform getEntityCullingSize() {
        return ENTITY_CULLING_SIZE;
    }
    @Override
    public Uniform getFrustumPos() {
        return FRUSTUM_POS;
    }
    @Override
    public Uniform getCullingViewMat() {
        return CULLING_VIEW_MAT;
    }
    @Override
    public Uniform getCullingProjMat() {
        return CULLING_PROJ_MAT;
    }


    @Mixin(RenderSystem.class)
    public static class MixinRenderSystem {
        @Inject(at = @At(value = "TAIL"), method = "setupShaderLights")
        private static void shader(ShaderInstance p_157462_, CallbackInfo ci) {
            CullingRenderEvent.setUniform(p_157462_);
        }
    }
}

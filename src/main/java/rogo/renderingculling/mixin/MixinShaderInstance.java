package rogo.renderingculling.mixin;

import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.CullingRenderEvent;
import rogo.renderingculling.api.impl.ICullingShader;

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
    public Uniform CULLING_CAMERA_DIR;
    @Nullable
    public Uniform BOX_SCALE;
    @Nullable
    public Uniform RENDER_DISTANCE;
    @Nullable
    public Uniform DEPTH_SIZE;
    @Nullable
    public Uniform CULLING_SIZE;
    @Nullable
    public Uniform ENTITY_CULLING_SIZE;
    @Nullable
    public Uniform LEVEL_HEIGHT_OFFSET;
    @Nullable
    public Uniform LEVEL_MIN_SECTION;
    @Nullable
    public Uniform CULLING_FRUSTUM;
    @Nullable
    public Uniform FRUSTUM_POS;
    @Nullable
    public Uniform CULLING_VIEW_MAT;
    @Nullable
    public Uniform CULLING_PROJ_MAT;

    @Final
    @Shadow
    private int programId;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V")
    public void construct(CallbackInfo ci) {
        this.CULLING_CAMERA_POS = this.getUniform("CullingCameraPos");
        this.CULLING_CAMERA_DIR = this.getUniform("CullingCameraDir");
        this.BOX_SCALE = this.getUniform("BoxScale");
        this.RENDER_DISTANCE = this.getUniform("RenderDistance");
        this.DEPTH_SIZE = this.getUniform("DepthSize");
        this.CULLING_SIZE = this.getUniform("CullingSize");
        this.LEVEL_HEIGHT_OFFSET = this.getUniform("LevelHeightOffset");
        this.LEVEL_MIN_SECTION = this.getUniform("LevelMinSection");
        this.ENTITY_CULLING_SIZE = this.getUniform("EntityCullingSize");
        this.CULLING_FRUSTUM = this.getUniform("CullingFrustum");
        this.FRUSTUM_POS = this.getUniform("FrustumPos");
        this.CULLING_VIEW_MAT = this.getUniform("CullingViewMat");
        this.CULLING_PROJ_MAT = this.getUniform("CullingProjMat");
    }

    @Override
    public Uniform getCullingFrustum() {
        return CULLING_FRUSTUM;
    }

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
    public Uniform getLevelHeightOffset() {
        return LEVEL_HEIGHT_OFFSET;
    }

    @Override
    public Uniform getLevelMinSection() {
        return LEVEL_MIN_SECTION;
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

    @Override
    public Uniform getCullingCameraDir() {
        return CULLING_CAMERA_DIR;
    }

    @Override
    public Uniform getBoxScale() {
        return BOX_SCALE;
    }

    @Inject(at = @At("TAIL"), method = "apply")
    public void onApply(CallbackInfo ci) {
        if (CullingHandler.updatingDepth)
            ProgramManager.glUseProgram(this.programId);
    }

    @Mixin(RenderSystem.class)
    public static class MixinRenderSystem {
        @Inject(at = @At(value = "TAIL"), method = "setupShaderLights")
        private static void shader(ShaderInstance p_157462_, CallbackInfo ci) {
            CullingRenderEvent.setUniform(p_157462_);
        }
    }
}

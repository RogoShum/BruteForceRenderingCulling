package rogo.renderingculling.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.CullingRenderEvent;
import rogo.renderingculling.api.ICullingShader;


@Mixin(Shader.class)
public abstract class MixinShaderInstance implements ICullingShader {
    @Shadow
    @Nullable
    public GlUniform getUniform(String p_173349_) {
        return null;
    }

    @Nullable
    public GlUniform CULLING_CAMERA_POS;
    @Nullable
    public GlUniform RENDER_DISTANCE;
    @Nullable
    public GlUniform DEPTH_SIZE;
    @Nullable
    public GlUniform CULLING_SIZE;
    @Nullable
    public GlUniform ENTITY_CULLING_SIZE;
    @Nullable
    public GlUniform LEVEL_HEIGHT_OFFSET;
    @Nullable
    public GlUniform LEVEL_MIN_SECTION;
    @Nullable
    public GlUniform CULLING_FRUSTUM;
    @Nullable
    public GlUniform FRUSTUM_POS;
    @Nullable
    public GlUniform CULLING_VIEW_MAT;
    @Nullable
    public GlUniform CULLING_PROJ_MAT;

    @Final
    @Shadow
    private int programId;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void construct(CallbackInfo ci) {
        this.CULLING_CAMERA_POS = this.getUniform("CullingCameraPos");
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
    public GlUniform getCullingFrustum() {return CULLING_FRUSTUM;}
    @Override
    public GlUniform getCullingCameraPos() {
        return CULLING_CAMERA_POS;
    }

    @Override
    public GlUniform getRenderDistance() {
        return RENDER_DISTANCE;
    }

    @Override
    public GlUniform getDepthSize() {
        return DEPTH_SIZE;
    }
    @Override
    public GlUniform getCullingSize() {
        return CULLING_SIZE;
    }
    @Override
    public GlUniform getLevelHeightOffset() {
        return LEVEL_HEIGHT_OFFSET;
    }
    @Override
    public GlUniform getLevelMinSection() {
        return LEVEL_MIN_SECTION;
    }
    @Override
    public GlUniform getEntityCullingSize() {
        return ENTITY_CULLING_SIZE;
    }
    @Override
    public GlUniform getFrustumPos() {
        return FRUSTUM_POS;
    }
    @Override
    public GlUniform getCullingViewMat() {
        return CULLING_VIEW_MAT;
    }
    @Override
    public GlUniform getCullingProjMat() {
        return CULLING_PROJ_MAT;
    }

    @Inject(at = @At("TAIL"), method = "bind")
    public void onApply(CallbackInfo ci) {
        if(CullingHandler.updatingDepth)
            GlProgramManager.useProgram(this.programId);
    }

    @Mixin(RenderSystem.class)
    public static class MixinRenderSystem {
        @Inject(at = @At(value = "TAIL"), method = "setupShaderLights")
        private static void shader(Shader p_157462_, CallbackInfo ci) {
            CullingRenderEvent.setUniform(p_157462_);
        }
    }
}

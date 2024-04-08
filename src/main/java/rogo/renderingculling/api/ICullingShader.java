package rogo.renderingculling.api;


import net.minecraft.client.gl.GlUniform;

public interface ICullingShader {
    GlUniform getRenderDistance();
    GlUniform getCullingCameraPos();
    GlUniform getDepthSize();
    GlUniform getCullingSize();
    GlUniform getLevelHeightOffset();
    GlUniform getLevelMinSection();
    GlUniform getEntityCullingSize();
    GlUniform getCullingFrustum();
    GlUniform getFrustumPos();
    GlUniform getCullingViewMat();
    GlUniform getCullingProjMat();
}
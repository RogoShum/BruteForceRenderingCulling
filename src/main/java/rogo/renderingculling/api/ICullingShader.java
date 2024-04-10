package rogo.renderingculling.api;

import com.mojang.blaze3d.shaders.Uniform;

public interface ICullingShader {
    Uniform getRenderDistance();
    Uniform getCullingCameraPos();
    Uniform getDepthSize();
    Uniform getCullingSize();
    Uniform getLevelHeightOffset();
    Uniform getLevelMinSection();
    Uniform getEntityCullingSize();
    Uniform getCullingFrustum();
    Uniform getFrustumPos();
    Uniform getCullingViewMat();
    Uniform getCullingProjMat();
}
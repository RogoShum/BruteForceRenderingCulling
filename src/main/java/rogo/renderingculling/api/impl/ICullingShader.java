package rogo.renderingculling.api.impl;

import com.mojang.blaze3d.shaders.Uniform;

public interface ICullingShader {
    Uniform getRenderDistance();
    Uniform getCullingCameraPos();
    Uniform getCullingCameraDir();
    Uniform getCullingFov();
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
package rogo.renderingculling.api.impl;

import com.mojang.blaze3d.shaders.Uniform;

public interface ICullingShader {
    Uniform getRenderDistance();
    Uniform getCullingCameraPos();
    Uniform getCullingCameraDir();
    Uniform getBoxScale();
    Uniform getDepthOffset();
    Uniform getDepthSize();
    Uniform getCullingSize();
    Uniform getLevelHeightOffset();
    Uniform getLevelMinSection();
    Uniform getEntityCullingSize();
    Uniform getCullingFrustum();
    Uniform getFrustumPos();
    Uniform getCullingViewMat();
    Uniform getCullingProjMat();
    Uniform getTestPos();
}
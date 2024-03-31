package rogo.renderingculling;

public interface ShaderLoader {
    int getDepthBuffer();

    boolean renderingShader();

    void bindDefaultFrameBuffer();
}

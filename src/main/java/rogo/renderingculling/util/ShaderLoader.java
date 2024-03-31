package rogo.renderingculling.util;

public interface ShaderLoader {
    int getFrameBufferID();

    boolean renderingShader();

    void bindDefaultFrameBuffer();
}

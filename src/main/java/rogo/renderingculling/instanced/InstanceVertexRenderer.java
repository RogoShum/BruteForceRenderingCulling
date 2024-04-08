package rogo.renderingculling.instanced;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL31;
import rogo.renderingculling.api.CullingRenderEvent;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL15.glBindBuffer;

public class InstanceVertexRenderer implements AutoCloseable {
    protected final VertexAttrib main;
    protected final VertexAttrib update;
    private int arrayObjectId;
    protected int indexCount;
    protected int instanceCount;
    protected final VertexAttrib mainAttrib;
    protected final VertexFormat.DrawMode mode;
    private VertexFormat.IntType indexType;
    private boolean updating = false;

    public InstanceVertexRenderer(VertexFormat.DrawMode mode, VertexAttrib mainAttrib, Consumer<FloatBuffer> consumer, VertexAttrib update) {
        this.main = mainAttrib;
        this.update = update;
        RenderSystem.glGenVertexArrays((p_166881_) -> {
            this.arrayObjectId = p_166881_;
        });
        this.mode = mode;
        this.mainAttrib = mainAttrib;
        init(consumer);
        this.indexCount = mode.getSize(mainAttrib.vertexCount());
    }

    public void init(Consumer<FloatBuffer> buffer) {
        bindVertexArray();
        mainAttrib.bind();
        mainAttrib.init(buffer);
        glBindBuffer(34962, 0);
        unbindVertexArray();
    }

    public void bind() {
        RenderSystem.glBindBuffer(34963, () -> {
            RenderSystem.IndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
            this.indexType = rendersystem$autostorageindexbuffer.getElementFormat();
            return rendersystem$autostorageindexbuffer.getId();
        });
    }

    public void addInstanceAttrib(Consumer<FloatBuffer> consumer) {
        if(!updating) {
            update.bind();
            updating = true;
        }
        update.addAttrib(consumer);
        instanceCount++;
    }

    public void unbind() {
        glBindBuffer(34962, 0);
    }

    public void enableVertexAttribArray() {
        update.enableVertexAttribArray();
        main.enableVertexAttribArray();
    }

    public void disableVertexAttribArray() {
        main.disableVertexAttribArray();
        update.disableVertexAttribArray();
    }

    private void bindVertexArray() {
        RenderSystem.glBindVertexArray(() -> this.arrayObjectId);
    }

    public static void unbindVertexArray() {
        RenderSystem.glBindVertexArray(() -> 0);
    }

    public void drawWithShader(Shader p_166870_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                this._drawWithShader(p_166870_);
            });
        } else {
            this._drawWithShader(p_166870_);
        }
    }

    public void _drawWithShader(Shader p_166879_) {
        if (this.indexCount != 0 && this.instanceCount > 0) {
            RenderSystem.assertOnRenderThread();
            BufferRenderer.unbindAll();

            for(int i = 0; i < 12; ++i) {
                int j = RenderSystem.getShaderTexture(i);
                p_166879_.addSampler("Sampler" + i, j);
            }

            if (p_166879_.modelViewMat != null) {
                p_166879_.modelViewMat.set(RenderSystem.getModelViewMatrix());
            }

            if (p_166879_.projectionMat != null) {
                p_166879_.projectionMat.set(RenderSystem.getProjectionMatrix());
            }

            if (p_166879_.viewRotationMat != null) {
                p_166879_.viewRotationMat.method_39978(RenderSystem.getInverseViewRotationMatrix());
            }

            if (p_166879_.colorModulator != null) {
                p_166879_.colorModulator.set(RenderSystem.getShaderColor());
            }

            if (p_166879_.fogStart != null) {
                p_166879_.fogStart.set(RenderSystem.getShaderFogStart());
            }

            if (p_166879_.fogEnd != null) {
                p_166879_.fogEnd.set(RenderSystem.getShaderFogEnd());
            }

            if (p_166879_.fogColor != null) {
                p_166879_.fogColor.set(RenderSystem.getShaderFogColor());
            }

            if (p_166879_.fogShape != null) {
                p_166879_.fogShape.set(RenderSystem.getShaderFogShape().getId());
            }

            if (p_166879_.textureMat != null) {
                p_166879_.textureMat.set(RenderSystem.getTextureMatrix());
            }

            if (p_166879_.gameTime != null) {
                p_166879_.gameTime.set(RenderSystem.getShaderGameTime());
            }

            if (p_166879_.screenSize != null) {
                Window window = MinecraftClient.getInstance().getWindow();
                p_166879_.screenSize.set((float)window.getWidth(), (float)window.getHeight());
            }

            if (p_166879_.lineWidth != null && (this.mode == VertexFormat.DrawMode.LINES || this.mode == VertexFormat.DrawMode.LINE_STRIP)) {
                p_166879_.lineWidth.set(RenderSystem.getShaderLineWidth());
            }

            CullingRenderEvent.setUniform(p_166879_);
            RenderSystem.setupShaderLights(p_166879_);

            bindVertexArray();
            bind();

            enableVertexAttribArray();
            p_166879_.bind();
            GL31.glDrawElementsInstanced(this.mode.mode, this.indexCount, this.indexType.type, 0 , this.instanceCount);
            p_166879_.unbind();
            disableVertexAttribArray();

            unbind();
            unbindVertexArray();
            this.instanceCount = 0;
            this.updating = false;
        }
    }

    public void close() {
        main.close();
        update.close();

        if (this.arrayObjectId > 0) {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = 0;
        }
    }
}
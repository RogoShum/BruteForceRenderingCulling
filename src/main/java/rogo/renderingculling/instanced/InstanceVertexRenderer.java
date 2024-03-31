package rogo.renderingculling.instanced;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL31;
import rogo.renderingculling.api.CullingRenderEvent;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL15.glBindBuffer;
@OnlyIn(Dist.CLIENT)
public class InstanceVertexRenderer implements AutoCloseable {
    protected final VertexAttrib main;
    protected final VertexAttrib update;
    private int arrayObjectId;
    protected int indexCount;
    protected int instanceCount;
    protected final VertexAttrib mainAttrib;
    protected final VertexFormat.Mode mode;
    private VertexFormat.IndexType indexType;
    private boolean updating = false;

    public InstanceVertexRenderer(VertexFormat.Mode mode, VertexAttrib mainAttrib, Consumer<FloatBuffer> consumer, VertexAttrib update) {
        this.main = mainAttrib;
        this.update = update;
        RenderSystem.glGenVertexArrays((p_166881_) -> {
            this.arrayObjectId = p_166881_;
        });
        this.mode = mode;
        this.mainAttrib = mainAttrib;
        init(consumer);
        this.indexCount = mode.indexCount(mainAttrib.vertexCount());
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
            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
            this.indexType = rendersystem$autostorageindexbuffer.type();
            return rendersystem$autostorageindexbuffer.name();
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

    public void drawWithShader(ShaderInstance p_166870_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                this._drawWithShader(p_166870_);
            });
        } else {
            this._drawWithShader(p_166870_);
        }
    }

    public void _drawWithShader(ShaderInstance p_166879_) {
        if (this.indexCount != 0 && this.instanceCount > 0) {
            RenderSystem.assertOnRenderThread();
            BufferUploader.reset();

            for(int i = 0; i < 12; ++i) {
                int j = RenderSystem.getShaderTexture(i);
                p_166879_.setSampler("Sampler" + i, j);
            }

            if (p_166879_.MODEL_VIEW_MATRIX != null) {
                p_166879_.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            }

            if (p_166879_.PROJECTION_MATRIX != null) {
                p_166879_.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
            }

            if (p_166879_.INVERSE_VIEW_ROTATION_MATRIX != null) {
                p_166879_.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
            }

            if (p_166879_.COLOR_MODULATOR != null) {
                p_166879_.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
            }

            if (p_166879_.FOG_START != null) {
                p_166879_.FOG_START.set(RenderSystem.getShaderFogStart());
            }

            if (p_166879_.FOG_END != null) {
                p_166879_.FOG_END.set(RenderSystem.getShaderFogEnd());
            }

            if (p_166879_.FOG_COLOR != null) {
                p_166879_.FOG_COLOR.set(RenderSystem.getShaderFogColor());
            }

            if (p_166879_.FOG_SHAPE != null) {
                p_166879_.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
            }

            if (p_166879_.TEXTURE_MATRIX != null) {
                p_166879_.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }

            if (p_166879_.GAME_TIME != null) {
                p_166879_.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }

            if (p_166879_.SCREEN_SIZE != null) {
                Window window = Minecraft.getInstance().getWindow();
                p_166879_.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
            }

            if (p_166879_.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
                p_166879_.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }

            CullingRenderEvent.setUniform(p_166879_);
            RenderSystem.setupShaderLights(p_166879_);

            bindVertexArray();
            bind();

            enableVertexAttribArray();
            p_166879_.apply();
            GL31.glDrawElementsInstanced(this.mode.asGLMode, this.indexCount, this.indexType.asGLType, 0 , this.instanceCount);
            p_166879_.clear();
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
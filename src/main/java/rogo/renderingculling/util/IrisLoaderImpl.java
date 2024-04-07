package rogo.renderingculling.util;
/*
import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;


public class IrisLoaderImpl implements ShaderLoader {
    @Override
    public int getFrameBufferID() {
        if (Iris.getPipelineManager().getPipeline().isPresent()) {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline().get();
            try {
                Field f = null;
                if (pipeline instanceof NewWorldRenderingPipeline) {
                    f = NewWorldRenderingPipeline.class.getDeclaredField("sodiumTerrainPipeline");
                }
                if (f != null) {
                    f.setAccessible(true);
                    SodiumTerrainPipeline sodiumTerrainPipeline = (SodiumTerrainPipeline) f.get(pipeline);
                    GlFramebuffer glFramebuffer = sodiumTerrainPipeline.getTerrainFramebuffer();
                    return glFramebuffer.getId();
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {

            }
        }

        return Minecraft.getInstance().getMainRenderTarget().frameBufferId;
    }

    @Override
    public boolean renderingShader() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    @Override
    public void bindDefaultFrameBuffer() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
}
 */
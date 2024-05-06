package rogo.renderingculling.util;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;


public class IrisLoaderImpl implements ShaderLoader {
    @Override
    public int getFrameBufferID() {
        if (Iris.getPipelineManager().getPipeline().isPresent()) {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline().get();
            try {
                Field f = null;
                if (pipeline instanceof IrisRenderingPipeline) {
                    f = IrisRenderingPipeline.class.getDeclaredField("sodiumTerrainPipeline");
                }
                if (f != null) {
                    f.setAccessible(true);
                    SodiumTerrainPipeline sodiumTerrainPipeline = (SodiumTerrainPipeline) f.get(pipeline);
                    GlFramebuffer glFramebuffer = sodiumTerrainPipeline.getTerrainSolidFramebuffer();
                    return glFramebuffer.getId();
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {

            }
        }

        return Minecraft.getInstance().getMainRenderTarget().frameBufferId;
    }

    @Override
    public boolean renderingShaderPass() {
        return IrisApi.getInstance().isRenderingShadowPass();
    }

    @Override
    public boolean enabledShader() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    @Override
    public void bindDefaultFrameBuffer() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
}
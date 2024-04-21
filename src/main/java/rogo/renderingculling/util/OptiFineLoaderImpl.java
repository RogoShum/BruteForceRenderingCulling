package rogo.renderingculling.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import rogo.renderingculling.api.CullingHandler;

import java.lang.reflect.Field;

public class OptiFineLoaderImpl implements ShaderLoader {
    public static Class<?> glState = null;

    @Override
    public int getFrameBufferID() {
        try {
            Field f = CullingHandler.OptiFine.getDeclaredField("dfb");
            f.setAccessible(true);
            Object dfb = f.get(null);
            Field buffer = dfb.getClass().getDeclaredField("glFramebuffer");
            buffer.setAccessible(true);
            return (int) buffer.get(dfb);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.fillInStackTrace();
        }

        return Minecraft.getInstance().getMainRenderTarget().frameBufferId;
    }

    @Override
    public boolean renderingShaderPass() {
        try {
            Field field = CullingHandler.OptiFine.getDeclaredField("shaderPackLoaded");
            field.setAccessible(true);
            return (Boolean) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.fillInStackTrace();
        }
        return false;
    }

    @Override
    public boolean enabledShader() {
        return renderingShaderPass();
    }

    @Override
    public void bindDefaultFrameBuffer() {
        if (glState == null) {
            try {
                glState = Class.forName("net.optifine.shaders.GlState");
            } catch (ClassNotFoundException e) {
                CullingHandler.LOGGER.debug("GlState Not Found");
            }
        }
        try {
            Field field = glState.getDeclaredField("activeFramebuffer");
            field.setAccessible(true);
            Object buffer = field.get(null);
            Field glFramebuffer = buffer.getClass().getDeclaredField("glFramebuffer");
            glFramebuffer.setAccessible(true);
            GlStateManager._glBindFramebuffer(36160, (int) glFramebuffer.get(buffer));
            GlStateManager._viewport(0, 0, Minecraft.getInstance().getMainRenderTarget().viewWidth, Minecraft.getInstance().getMainRenderTarget().viewHeight);
            return;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.fillInStackTrace();
        }
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
}

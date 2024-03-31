package rogo.renderingculling;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class OptiFineLoaderImpl implements ShaderLoader {
    public static Class<?> glState = null;

    @Override
    public int getDepthBuffer() {
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
    public boolean renderingShader() {
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
            return;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.fillInStackTrace();
        }
        GlStateManager._glBindFramebuffer(36160, 0);
    }
}

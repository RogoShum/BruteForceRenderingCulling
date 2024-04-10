package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import rogo.renderingculling.instanced.EntityCullingInstanceRenderer;
import rogo.renderingculling.mixin.AccessorFrustum;

import java.util.ArrayList;
import java.util.List;

public class CullingRenderEvent {
    public static CullingRenderEvent INSTANCE;
    public int fontCount = 0;
    public static EntityCullingInstanceRenderer ENTITY_CULLING_INSTANCE_RENDERER;
    static {
        RenderSystem.recordRenderCall(() -> ENTITY_CULLING_INSTANCE_RENDERER = new EntityCullingInstanceRenderer());
    }

    static {
        INSTANCE = new CullingRenderEvent();
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> INSTANCE.onOverlayRender(matrixStack, tickDelta));
    }

    public void onOverlayRender(GuiGraphics guiGraphics, float tickDelta ) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (CullingHandler.INSTANCE.DEBUG) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = minecraft.getWindow().getGuiScaledWidth() / 2;
            int height = 12;
            int widthScale = 80;

            int heightScale = -minecraft.font.lineHeight * fontCount;
            fontCount = 0;
            if(CullingHandler.INSTANCE.fps == 0 ) {
                CullingHandler.INSTANCE.fps++;
            }

            if(CullingHandler.INSTANCE.cullingInitCount == 0 ) {
                CullingHandler.INSTANCE.cullingInitCount++;
            }

            if (Config.getCullChunk()) {
                String cullingInitTime = Component.translatable("brute_force_rendering_culling.chunk_culling_init").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(guiGraphics, cullingInitTime, width, height - heightScale);

                String chunkCullingTime = Component.translatable("brute_force_rendering_culling.chunk_culling_time").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(guiGraphics, chunkCullingTime, width, height - heightScale);
            }

            String chunkCulling = Component.translatable("brute_force_rendering_culling.chunk_culling").getString() + ": " + CullingHandler.INSTANCE.chunkCulling + " / " + CullingHandler.INSTANCE.chunkCount;
            drawString(guiGraphics, chunkCulling, width, height - heightScale);

            if (Config.getCullEntity()) {
                String initTime = Component.translatable("brute_force_rendering_culling.entity_culling_init").getString() + ": " + (CullingHandler.INSTANCE.entityCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(guiGraphics, initTime, width, height - heightScale);

                String blockCullingTime = Component.translatable("brute_force_rendering_culling.block_culling_time").getString() + ": " + (CullingHandler.INSTANCE.blockCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(guiGraphics, blockCullingTime, width, height - heightScale);

                String blockCulling = Component.translatable("brute_force_rendering_culling.block_culling").getString() + ": " + CullingHandler.INSTANCE.blockCulling + " / " + CullingHandler.INSTANCE.blockCount;
                drawString(guiGraphics, blockCulling, width, height - heightScale);

                String entityCullingTime = Component.translatable("brute_force_rendering_culling.entity_culling_time").getString() + ": " + (CullingHandler.INSTANCE.entityCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(guiGraphics, entityCullingTime, width, height - heightScale);

                String entityCulling = Component.translatable("brute_force_rendering_culling.entity_culling").getString() + ": " + CullingHandler.INSTANCE.entityCulling + " / " + CullingHandler.INSTANCE.entityCount;
                drawString(guiGraphics, entityCulling, width, height - heightScale);
            }

            String Sampler = Component.translatable("brute_force_rendering_culling.sampler").getString() + ": " + String.valueOf((Float.parseFloat(String.format("%.0f", Config.getSampling() * 100.0D))) + "%");
            drawString(guiGraphics, Sampler, width, height - heightScale);

            String cull_chunk = Component.translatable("brute_force_rendering_culling.cull_chunk").getString() + ": "
                    + (Config.getCullChunk() ? Component.translatable("brute_force_rendering_culling.enable").getString() : Component.translatable("brute_force_rendering_culling.disable").getString());
            drawString(guiGraphics, cull_chunk, width, height - heightScale);

            String cull = Component.translatable("brute_force_rendering_culling.cull_entity").getString() + ": "
                    + (Config.getCullEntity() ? Component.translatable("brute_force_rendering_culling.enable").getString() : Component.translatable("brute_force_rendering_culling.disable").getString());
            drawString(guiGraphics, cull, width, height - heightScale);

            int index = Minecraft.getInstance().fpsString.indexOf("fps");
            if (index != -1) {
                String extractedString = Minecraft.getInstance().fpsString.substring(0, index+3);
                String fps = "FPS: " + extractedString;
                drawString(guiGraphics, fps, width, height - heightScale);
            }

            height -= heightScale - minecraft.font.lineHeight;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width + widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width + widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width - widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            BufferUploader.draw(bufferbuilder.end());
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale - 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width - widthScale - 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();

            BufferUploader.draw(bufferbuilder.end());
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();

            if(!CullingHandler.INSTANCE.checkTexture)
                return;

            Tesselator tessellator = Tesselator.getInstance();
            float screenScale = 1.0f;
            double windowScale = 0.4;
            for(int i = 0; i < CullingHandler.DEPTH_BUFFER_TARGET.length; ++i) {
                int scaledHeight = (int) (minecraft.getWindow().getGuiScaledHeight() * windowScale * screenScale);
                int scaledWidth = (int) (minecraft.getWindow().getGuiScaledWidth() * windowScale * screenScale);
                int offsetHeight = (int) ((1-screenScale)* 2 * minecraft.getWindow().getGuiScaledHeight() * windowScale);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight()-offsetHeight, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(scaledWidth, minecraft.getWindow().getGuiScaledHeight()-offsetHeight, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(scaledWidth, minecraft.getWindow().getGuiScaledHeight()-scaledHeight-offsetHeight, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight()-scaledHeight-offsetHeight, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingHandler.DEPTH_TEXTURE[i]);
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
                screenScale *= 0.5f;
            }

            if(Config.getCullEntity()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth()-height, height, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)minecraft.getWindow().getGuiScaledWidth(), height, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)minecraft.getWindow().getGuiScaledWidth(), 0, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth()-height, 0, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingHandler.ENTITY_CULLING_MAP_TARGET.getColorTextureId());
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }

            if(Config.getCullChunk()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth()-height, height*2, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)minecraft.getWindow().getGuiScaledWidth(), height*2, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)minecraft.getWindow().getGuiScaledWidth(), height, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth()-height, height, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingHandler.CHUNK_CULLING_MAP_TARGET.getColorTextureId());
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }
        }
    }

    public void drawString(GuiGraphics guiGraphics, String text, int width, int height) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, text, (int) (width - (font.width(text) / 2f)), height - font.lineHeight * fontCount, 16777215);
        fontCount++;
    }

    protected static void onUpdateCullingMap() {
        if(!CullingHandler.anyCulling())
            return;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        if(CullingHandler.INSTANCE.checkCulling)
            return;

        if(Config.getCullEntity() && CullingHandler.ENTITY_CULLING_MAP != null && CullingHandler.ENTITY_CULLING_MAP.needTransferData()) {
            CullingHandler.ENTITY_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingHandler.ENTITY_CULLING_MAP_TARGET.bindWrite(false);
            CullingHandler.callDepthTexture();
            CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addEntityAttribute(ENTITY_CULLING_INSTANCE_RENDERER::addInstanceAttrib);
            ENTITY_CULLING_INSTANCE_RENDERER.drawWithShader(CullingHandler.INSTANCED_ENTITY_CULLING_SHADER);
        }

        if(Config.getCullChunk() && CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.needTransferData()) {
            CullingHandler.useShader(CullingHandler.CHUNK_CULLING_SHADER);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.bindWrite(false);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f,  1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).endVertex();
            CullingHandler.callDepthTexture();
            tessellator.end();
        }

        CullingHandler.bindMainFrameTarget();
    }

    public static void setUniform(ShaderInstance shader) {
        ICullingShader shaderInstance = (ICullingShader) shader;
        if(shaderInstance.getCullingCameraPos() != null) {
            Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            float[] array = new float[]{(float) pos.x, (float) pos.y, (float) pos.z};
            shaderInstance.getCullingCameraPos().set(array);
        }
        if(shaderInstance.getFrustumPos() != null) {
            Vec3 pos = new Vec3(
                    ((AccessorFrustum)CullingHandler.FRUSTUM).camX(),
                    ((AccessorFrustum)CullingHandler.FRUSTUM).camY(),
                    ((AccessorFrustum)CullingHandler.FRUSTUM).camZ());

            float[] array = new float[]{(float) pos.x, (float) pos.y, (float) pos.z};
            shaderInstance.getFrustumPos().set(array);
        }
        if(shaderInstance.getCullingViewMat() != null) {
            shaderInstance.getCullingViewMat().set(CullingHandler.VIEW_MATRIX);
        }
        if(shaderInstance.getCullingProjMat() != null) {
            shaderInstance.getCullingProjMat().set(CullingHandler.PROJECTION_MATRIX);
        }
        if(shaderInstance.getCullingFrustum() != null) {
            Vector4f[] frustumData = ((AccessorFrustum.AccessorFrustumIntersection)((AccessorFrustum)CullingHandler.FRUSTUM).frustumIntersection()).planes();
            List<Float> data = new ArrayList<>();
            for (Vector4f frustumDatum : frustumData) {
                data.add(frustumDatum.x());
                data.add(frustumDatum.y());
                data.add(frustumDatum.z());
                data.add(frustumDatum.w());
            }
            float[] array = new float[data.size()];
            for (int i = 0; i < data.size(); i++) {
                array[i] = data.get(i);
            }
            shaderInstance.getCullingFrustum().set(array);
        }
        if(shaderInstance.getRenderDistance() != null) {
            float distance = Minecraft.getInstance().options.getEffectiveRenderDistance();
            if(shader == CullingHandler.COPY_DEPTH_SHADER) {
                if(CullingHandler.DEPTH_INDEX > 0)
                    distance = 2;
                else
                    distance = 0;
            }

            shaderInstance.getRenderDistance().set(distance);
        }
        if(shaderInstance.getDepthSize() != null) {
            float[] array = new float[CullingHandler.depthSize*2];
            if(shader == CullingHandler.COPY_DEPTH_SHADER) {
                array[0] = (float) CullingHandler.DEPTH_BUFFER_TARGET[CullingHandler.DEPTH_INDEX].width;
                array[1] = (float) CullingHandler.DEPTH_BUFFER_TARGET[CullingHandler.DEPTH_INDEX].height;
            } else {
                for(int i = 0; i < CullingHandler.depthSize; ++i) {
                    int arrayIdx = i*2;
                    array[arrayIdx] = (float) CullingHandler.DEPTH_BUFFER_TARGET[i].width;
                    array[arrayIdx+1] = (float) CullingHandler.DEPTH_BUFFER_TARGET[i].height;
                }
            }
            shaderInstance.getDepthSize().set(array);
        }
        if(shader == CullingHandler.COPY_DEPTH_SHADER && CullingHandler.DEPTH_INDEX > 0 && shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) CullingHandler.DEPTH_BUFFER_TARGET[CullingHandler.DEPTH_INDEX-1].width, (float) CullingHandler.DEPTH_BUFFER_TARGET[CullingHandler.DEPTH_INDEX-1].height);
        }
        if(shaderInstance.getCullingSize() != null) {
            shaderInstance.getCullingSize().set((float) CullingHandler.CHUNK_CULLING_MAP_TARGET.width, (float) CullingHandler.CHUNK_CULLING_MAP_TARGET.height);
        }
        if(shaderInstance.getEntityCullingSize() != null) {
            shaderInstance.getEntityCullingSize().set((float) CullingHandler.ENTITY_CULLING_MAP_TARGET.width, (float) CullingHandler.ENTITY_CULLING_MAP_TARGET.height);
        }
        if(shaderInstance.getLevelHeightOffset() != null) {
            shaderInstance.getLevelHeightOffset().set(CullingHandler.LEVEL_HEIGHT_OFFSET);
        }
        if(shaderInstance.getLevelMinSection() != null && Minecraft.getInstance().level != null) {
            shaderInstance.getLevelMinSection().set(Minecraft.getInstance().level.getMinSection());
        }
    }
}

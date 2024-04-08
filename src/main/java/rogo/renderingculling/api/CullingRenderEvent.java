package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
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

    public void onOverlayRender(MatrixStack matrixStack, float tickDelta ) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        if (CullingHandler.INSTANCE.DEBUG) {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            int width = minecraft.getWindow().getScaledWidth() / 2;
            int height = (int) (minecraft.getWindow().getScaledHeight() / 2.5);
            int widthScale = 80;

            int heightScale = -minecraft.textRenderer.fontHeight * fontCount;
            fontCount = 0;
            if(CullingHandler.INSTANCE.fps == 0 ) {
                CullingHandler.INSTANCE.fps++;
            }

            if(CullingHandler.INSTANCE.cullingInitCount == 0 ) {
                CullingHandler.INSTANCE.cullingInitCount++;
            }

            if (Config.CULL_CHUNK.getValue()) {
                String cullingInitTime = new TranslatableText("brute_force_rendering_culling.chunk_culling_init").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(cullingInitTime, width, height - heightScale);

                String chunkCullingTime = new TranslatableText("brute_force_rendering_culling.chunk_culling_time").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(chunkCullingTime, width, height - heightScale);
            }

            String chunkCulling = new TranslatableText("brute_force_rendering_culling.chunk_culling").getString() + ": " + CullingHandler.INSTANCE.chunkCulling + " / " + CullingHandler.INSTANCE.chunkCount;
            drawString(chunkCulling, width, height - heightScale);

            if (Config.CULL_ENTITY.getValue()) {
                String initTime = new TranslatableText("brute_force_rendering_culling.entity_culling_init").getString() + ": " + (CullingHandler.INSTANCE.entityCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(initTime, width, height - heightScale);

                String blockCullingTime = new TranslatableText("brute_force_rendering_culling.block_culling_time").getString() + ": " + (CullingHandler.INSTANCE.blockCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(blockCullingTime, width, height - heightScale);

                String blockCulling = new TranslatableText("brute_force_rendering_culling.block_culling").getString() + ": " + CullingHandler.INSTANCE.blockCulling + " / " + CullingHandler.INSTANCE.blockCount;
                drawString(blockCulling, width, height - heightScale);

                String entityCullingTime = new TranslatableText("brute_force_rendering_culling.entity_culling_time").getString() + ": " + (CullingHandler.INSTANCE.entityCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(entityCullingTime, width, height - heightScale);

                String entityCulling = new TranslatableText("brute_force_rendering_culling.entity_culling").getString() + ": " + CullingHandler.INSTANCE.entityCulling + " / " + CullingHandler.INSTANCE.entityCount;
                drawString(entityCulling, width, height - heightScale);
            }

            String Sampler = new TranslatableText("brute_force_rendering_culling.sampler").getString() + ": " + String.valueOf((Float.parseFloat(String.format("%.0f", Config.SAMPLING.getValue() * 100.0D))) + "%");
            drawString(Sampler, width, height - heightScale);

            String cull_chunk = new TranslatableText("brute_force_rendering_culling.cull_chunk").getString() + ": "
                    + (Config.CULL_CHUNK.getValue() ? new TranslatableText("brute_force_rendering_culling.enable").getString() : new TranslatableText("brute_force_rendering_culling.disable").getString());
            drawString(cull_chunk, width, height - heightScale);

            String cull = new TranslatableText("brute_force_rendering_culling.cull_entity").getString() + ": "
                    + (Config.CULL_ENTITY.getValue() ? new TranslatableText("brute_force_rendering_culling.enable").getString() : new TranslatableText("brute_force_rendering_culling.disable").getString());
            drawString(cull, width, height - heightScale);

            int index = MinecraftClient.getInstance().fpsDebugString.indexOf("fps");
            if (index != -1) {
                String extractedString = MinecraftClient.getInstance().fpsDebugString.substring(0, index+3);
                String fps = "FPS: " + extractedString;
                drawString(fps, width, height - heightScale);
            }

            height -= heightScale - minecraft.textRenderer.fontHeight;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_COLOR);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
            bufferbuilder.vertex(width + widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
            bufferbuilder.vertex(width + widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
            bufferbuilder.vertex(width - widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
            bufferbuilder.end();
            BufferRenderer.draw(bufferbuilder);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale - 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
            bufferbuilder.vertex(width + widthScale + 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
            bufferbuilder.vertex(width + widthScale + 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
            bufferbuilder.vertex(width - widthScale - 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
            bufferbuilder.end();
            BufferRenderer.draw(bufferbuilder);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();

            if(!CullingHandler.anyCulling())
                return;

            Tessellator tessellator = Tessellator.getInstance();
            height = (int) (minecraft.getWindow().getScaledHeight()*0.25f);
            width = (int) (minecraft.getWindow().getScaledWidth()*0.25f);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferbuilder.vertex(0.0D, minecraft.getWindow().getScaledHeight(), 0.0D).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
            bufferbuilder.vertex(width, minecraft.getWindow().getScaledHeight(), 0.0D).texture(1, 0.0F).color(255, 255, 255, 255).next();
            bufferbuilder.vertex(width, minecraft.getWindow().getScaledHeight()-height, 0.0D).texture(1, 1).color(255, 255, 255, 255).next();
            bufferbuilder.vertex(0.0D, minecraft.getWindow().getScaledHeight()-height, 0.0D).texture(0.0F, 1).color(255, 255, 255, 255).next();
            RenderSystem.setShaderTexture(0, CullingHandler.DEPTH_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();

            if(Config.CULL_ENTITY.getValue()) {
                height = (int) (minecraft.getWindow().getScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferbuilder.vertex(0.0D, height, 0.0D).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
                bufferbuilder.vertex((double)height, height, 0.0D).texture(1, 0.0F).color(255, 255, 255, 255).next();
                bufferbuilder.vertex((double)height, 0, 0.0D).texture(1, 1).color(255, 255, 255, 255).next();
                bufferbuilder.vertex(0.0D, 0, 0.0D).texture(0.0F, 1).color(255, 255, 255, 255).next();
                RenderSystem.setShaderTexture(0, CullingHandler.ENTITY_CULLING_MAP_TARGET.getColorAttachment());
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.draw();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }

            if(Config.CULL_CHUNK.getValue()) {
                height = (int) (minecraft.getWindow().getScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getScaledHeight()-height*1.5, 0.0D).texture(0.0F, 0.0F).color(255, 255, 255, 255).next();
                bufferbuilder.vertex((double)height, minecraft.getWindow().getScaledHeight()-height*1.5, 0.0D).texture(1, 0.0F).color(255, 255, 255, 255).next();
                bufferbuilder.vertex((double)height, (double)minecraft.getWindow().getScaledHeight()-height*2.5, 0.0D).texture(1, 1).color(255, 255, 255, 255).next();
                bufferbuilder.vertex(0.0D, (double)minecraft.getWindow().getScaledHeight()-height*2.5, 0.0D).texture(0.0F, 1).color(255, 255, 255, 255).next();
                RenderSystem.setShaderTexture(0, CullingHandler.CHUNK_CULLING_MAP_TARGET.getColorAttachment());
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.draw();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }
        }
    }

    public void drawString(String text, int width, int height) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        font.drawWithShadow(new MatrixStack(), text, width - (font.getWidth(text) / 2f), height - font.fontHeight * fontCount, 16777215);
        fontCount++;
    }

    protected static void onUpdateCullingMap() {
        if(!CullingHandler.anyCulling())
            return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        CullingHandler.useShader(CullingHandler.LINEARIZE_DEPTH_SHADER);
        if(CullingHandler.MONOCHROME_DEPTH_TARGET.textureWidth != CullingHandler.DEPTH_BUFFER_TARGET.textureWidth || CullingHandler.MONOCHROME_DEPTH_TARGET.textureHeight != CullingHandler.DEPTH_BUFFER_TARGET.textureHeight) {
            CullingHandler.MONOCHROME_DEPTH_TARGET.resize(CullingHandler.DEPTH_BUFFER_TARGET.textureWidth, CullingHandler.DEPTH_BUFFER_TARGET.textureHeight, MinecraftClient.IS_SYSTEM_MAC);
        }

        CullingHandler.MONOCHROME_DEPTH_TARGET.clear(MinecraftClient.IS_SYSTEM_MAC);
        CullingHandler.MONOCHROME_DEPTH_TARGET.beginWrite(false);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).next();
        bufferbuilder.vertex(1.0f, -1.0f, 0.0f).next();
        bufferbuilder.vertex(1.0f,  1.0f, 0.0f).next();
        bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).next();
        RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
        tessellator.draw();


        if(CullingHandler.INSTANCE.checkCulling)
            return;

        if(Config.CULL_ENTITY.getValue() && CullingHandler.ENTITY_CULLING_MAP != null && CullingHandler.ENTITY_CULLING_MAP.needTransferData()) {
            CullingHandler.ENTITY_CULLING_MAP_TARGET.clear(MinecraftClient.IS_SYSTEM_MAC);
            CullingHandler.ENTITY_CULLING_MAP_TARGET.beginWrite(false);
            RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
            CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addEntityAttribute(ENTITY_CULLING_INSTANCE_RENDERER::addInstanceAttrib);
            ENTITY_CULLING_INSTANCE_RENDERER.drawWithShader(CullingHandler.INSTANCED_ENTITY_CULLING_SHADER);
        }

        if(Config.CULL_CHUNK.getValue() && CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.needTransferData()) {
            CullingHandler.useShader(CullingHandler.CHUNK_CULLING_SHADER);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.clear(MinecraftClient.IS_SYSTEM_MAC);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.beginWrite(false);
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).next();
            bufferbuilder.vertex(1.0f, -1.0f, 0.0f).next();
            bufferbuilder.vertex(1.0f,  1.0f, 0.0f).next();
            bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).next();
            RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
            tessellator.draw();
        }
        CullingHandler.bindMainFrameTarget();
    }

    public static void setUniform(Shader shader) {
        ICullingShader shaderInstance = (ICullingShader) shader;
        if(shaderInstance.getCullingCameraPos() != null) {
            Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            float[] array = new float[]{(float) pos.x, (float) pos.y, (float) pos.z};
            shaderInstance.getCullingCameraPos().set(array);
        }
        if(shaderInstance.getFrustumPos() != null) {
            Vec3d pos = new Vec3d(
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
            Vector4f[] frustumData = ((AccessorFrustum)CullingHandler.FRUSTUM).frustumData();
            List<Float> data = new ArrayList<>();
            for (Vector4f frustumDatum : frustumData) {
                data.add(frustumDatum.getX());
                data.add(frustumDatum.getY());
                data.add(frustumDatum.getZ());
                data.add(frustumDatum.getW());
            }
            float[] array = new float[data.size()];
            for (int i = 0; i < data.size(); i++) {
                array[i] = data.get(i);
            }
            shaderInstance.getCullingFrustum().set(array);
        }
        if(shaderInstance.getRenderDistance() != null) {
            float distance = MinecraftClient.getInstance().options.getViewDistance();
            shaderInstance.getRenderDistance().set(distance);
        }
        if(shaderInstance.getDepthSize() != null) {
            shaderInstance.getDepthSize().set((float) CullingHandler.DEPTH_BUFFER_TARGET.textureWidth, (float) CullingHandler.DEPTH_BUFFER_TARGET.textureHeight);
        }
        if(shaderInstance.getCullingSize() != null) {
            shaderInstance.getCullingSize().set((float) CullingHandler.CHUNK_CULLING_MAP_TARGET.textureWidth, (float) CullingHandler.CHUNK_CULLING_MAP_TARGET.textureHeight);
        }
        if(shaderInstance.getEntityCullingSize() != null) {
            shaderInstance.getEntityCullingSize().set((float) CullingHandler.ENTITY_CULLING_MAP_TARGET.textureWidth, (float) CullingHandler.ENTITY_CULLING_MAP_TARGET.textureHeight);
        }
        if(shaderInstance.getLevelHeightOffset() != null) {
            shaderInstance.getLevelHeightOffset().set(CullingHandler.LEVEL_HEIGHT_OFFSET);
        }
        if(shaderInstance.getLevelMinSection() != null && MinecraftClient.getInstance().world != null) {
            shaderInstance.getLevelMinSection().set(MinecraftClient.getInstance().world.getBottomSectionCoord());
        }
    }
}

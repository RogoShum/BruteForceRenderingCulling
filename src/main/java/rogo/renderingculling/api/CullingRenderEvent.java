package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import rogo.renderingculling.instanced.EntityCullingInstanceRenderer;
import rogo.renderingculling.mixin.AccessorFrustum;

import java.util.ArrayList;
import java.util.List;

public class CullingRenderEvent {
    public int fontCount = 0;
    public static EntityCullingInstanceRenderer ENTITY_CULLING_INSTANCE_RENDERER;
    static {
        RenderSystem.recordRenderCall(() -> ENTITY_CULLING_INSTANCE_RENDERER = new EntityCullingInstanceRenderer());
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.PostLayer event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (CullingHandler.INSTANCE.DEBUG && event.getOverlay() == ForgeIngameGui.HELMET_ELEMENT) {
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

            if (Config.CULL_CHUNK.get()) {
                String cullingInitTime = new TranslatableComponent("brute_force_rendering_culling.chunk_culling_init").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(cullingInitTime, width, height - heightScale);

                String chunkCullingTime = new TranslatableComponent("brute_force_rendering_culling.chunk_culling_time").getString() + ": " + (CullingHandler.INSTANCE.chunkCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(chunkCullingTime, width, height - heightScale);
            }

            String chunkCulling = new TranslatableComponent("brute_force_rendering_culling.chunk_culling").getString() + ": " + CullingHandler.INSTANCE.chunkCulling + " / " + CullingHandler.INSTANCE.chunkCount;
            drawString(chunkCulling, width, height - heightScale);

            if (Config.CULL_ENTITY.get()) {
                String initTime = new TranslatableComponent("brute_force_rendering_culling.entity_culling_init").getString() + ": " + (CullingHandler.INSTANCE.entityCullingInitTime /1000/CullingHandler.INSTANCE.cullingInitCount) + " μs";
                drawString(initTime, width, height - heightScale);

                String blockCullingTime = new TranslatableComponent("brute_force_rendering_culling.block_culling_time").getString() + ": " + (CullingHandler.INSTANCE.blockCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(blockCullingTime, width, height - heightScale);

                String blockCulling = new TranslatableComponent("brute_force_rendering_culling.block_culling").getString() + ": " + CullingHandler.INSTANCE.blockCulling + " / " + CullingHandler.INSTANCE.blockCount;
                drawString(blockCulling, width, height - heightScale);

                String entityCullingTime = new TranslatableComponent("brute_force_rendering_culling.entity_culling_time").getString() + ": " + (CullingHandler.INSTANCE.entityCullingTime/1000/CullingHandler.INSTANCE.fps) + " μs";
                drawString(entityCullingTime, width, height - heightScale);

                String entityCulling = new TranslatableComponent("brute_force_rendering_culling.entity_culling").getString() + ": " + CullingHandler.INSTANCE.entityCulling + " / " + CullingHandler.INSTANCE.entityCount;
                drawString(entityCulling, width, height - heightScale);
            }

            String Sampler = new TranslatableComponent("brute_force_rendering_culling.sampler").getString() + ": " + String.valueOf((Float.parseFloat(String.format("%.0f", Config.SAMPLING.get() * 100.0D))) + "%");
            drawString(Sampler, width, height - heightScale);

            String cull_chunk = new TranslatableComponent("brute_force_rendering_culling.cull_chunk").getString() + ": "
                    + (Config.CULL_CHUNK.get() ? new TranslatableComponent("brute_force_rendering_culling.enable").getString() : new TranslatableComponent("brute_force_rendering_culling.disable").getString());
            drawString(cull_chunk, width, height - heightScale);

            String cull = new TranslatableComponent("brute_force_rendering_culling.cull_entity").getString() + ": "
                    + (Config.CULL_ENTITY.get() ? new TranslatableComponent("brute_force_rendering_culling.enable").getString() : new TranslatableComponent("brute_force_rendering_culling.disable").getString());
            drawString(cull, width, height - heightScale);

            int index = Minecraft.getInstance().fpsString.indexOf("fps");
            if (index != -1) {
                String extractedString = Minecraft.getInstance().fpsString.substring(0, index+3);
                String fps = "FPS: " + extractedString;
                drawString(fps, width, height - heightScale);
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
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale - 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width - widthScale - 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();

            if(!CullingHandler.anyCulling())
                return;

            Tesselator tessellator = Tesselator.getInstance();
            height = (int) (minecraft.getWindow().getGuiScaledHeight());
            width = (int) (minecraft.getWindow().getGuiScaledWidth());
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight(), 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(width, minecraft.getWindow().getGuiScaledHeight(), 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(width, minecraft.getWindow().getGuiScaledHeight()-height, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight()-height, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
            RenderSystem.setShaderTexture(0, CullingHandler.MONOCHROME_DEPTH_TARGET.getColorTextureId());
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            tessellator.end();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();

            if(Config.CULL_ENTITY.get()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(0.0D, height, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)height, height, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)height, 0, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(0.0D, 0, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingHandler.ENTITY_CULLING_MAP_TARGET.getColorTextureId());
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                tessellator.end();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();
            }

            if(Config.CULL_CHUNK.get()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight()*0.25f);
                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight()-height*1.5, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)height, minecraft.getWindow().getGuiScaledHeight()-height*1.5, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double)height, (double)minecraft.getWindow().getGuiScaledHeight()-height*2.5, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(0.0D, (double)minecraft.getWindow().getGuiScaledHeight()-height*2.5, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
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

    public void drawString(String text, int width, int height) {
        Font font = Minecraft.getInstance().font;
        font.drawShadow(new PoseStack(), text, width - (font.width(text) / 2f), height - font.lineHeight * fontCount, 16777215);
        fontCount++;
    }

    protected static void onUpdateCullingMap() {
        if(!CullingHandler.anyCulling())
            return;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        CullingHandler.useShader(CullingHandler.SPACE_DEPTH_SHADER);
        if(CullingHandler.MONOCHROME_DEPTH_TARGET.width != Minecraft.getInstance().getMainRenderTarget().width || CullingHandler.MONOCHROME_DEPTH_TARGET.height != Minecraft.getInstance().getMainRenderTarget().height) {
            CullingHandler.MONOCHROME_DEPTH_TARGET.resize(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, Minecraft.ON_OSX);
        }

        CullingHandler.MONOCHROME_DEPTH_TARGET.clear(Minecraft.ON_OSX);
        CullingHandler.MONOCHROME_DEPTH_TARGET.bindWrite(false);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
        bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
        bufferbuilder.vertex(1.0f,  1.0f, 0.0f).endVertex();
        bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).endVertex();
        RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
        tessellator.end();


        if(CullingHandler.INSTANCE.checkCulling)
            return;

        if(Config.CULL_ENTITY.get() && CullingHandler.ENTITY_CULLING_MAP != null && CullingHandler.ENTITY_CULLING_MAP.needTransferData()) {
            CullingHandler.ENTITY_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingHandler.ENTITY_CULLING_MAP_TARGET.bindWrite(false);
            RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
            CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addEntityAttribute(ENTITY_CULLING_INSTANCE_RENDERER::addInstanceAttrib);
            ENTITY_CULLING_INSTANCE_RENDERER.drawWithShader(CullingHandler.INSTANCED_ENTITY_CULLING_SHADER);
        }

        if(Config.CULL_CHUNK.get() && CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.needTransferData()) {
            CullingHandler.useShader(CullingHandler.CHUNK_CULLING_SHADER);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingHandler.CHUNK_CULLING_MAP_TARGET.bindWrite(false);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f,  1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).endVertex();
            RenderSystem.setShaderTexture(7, CullingHandler.DEPTH_TEXTURE);
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
            Vector4f[] frustumData = ((AccessorFrustum)CullingHandler.FRUSTUM).frustumData();
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
            if(shader == CullingHandler.SPACE_DEPTH_SHADER) {
                shaderInstance.getRenderDistance().set(16f);
            } else {
                float distance = Minecraft.getInstance().options.getEffectiveRenderDistance();
                shaderInstance.getRenderDistance().set(distance);
            }
        }
        if(shaderInstance.getDepthSize() != null) {
            shaderInstance.getDepthSize().set((float) CullingHandler.DEPTH_BUFFER_TARGET.width, (float) CullingHandler.DEPTH_BUFFER_TARGET.height);
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

package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import rogo.renderingculling.api.impl.ICullingShader;
import rogo.renderingculling.instanced.EntityCullingInstanceRenderer;
import rogo.renderingculling.mixin.AccessorFrustum;

import java.util.ArrayList;
import java.util.List;

import static rogo.renderingculling.gui.ConfigScreen.u;
import static rogo.renderingculling.gui.ConfigScreen.v;

public class CullingRenderEvent {

    public static EntityCullingInstanceRenderer ENTITY_CULLING_INSTANCE_RENDERER;

    static {
        RenderSystem.recordRenderCall(() -> ENTITY_CULLING_INSTANCE_RENDERER = new EntityCullingInstanceRenderer());
    }

    float partialTick;

    protected static void updateCullingMap() {
        if (!CullingStateManager.anyCulling())
            return;

        if (CullingStateManager.checkCulling)
            return;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        CullingStateManager.callDepthTexture();

        if (Config.getCullEntity() && CullingStateManager.ENTITY_CULLING_MAP != null && CullingStateManager.ENTITY_CULLING_MAP.needTransferData()) {
            CullingStateManager.ENTITY_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingStateManager.ENTITY_CULLING_MAP_TARGET.bindWrite(false);
            CullingStateManager.ENTITY_CULLING_MAP.getEntityTable().addEntityAttribute(CullingRenderEvent.ENTITY_CULLING_INSTANCE_RENDERER::addInstanceAttrib);
            ENTITY_CULLING_INSTANCE_RENDERER.drawWithShader(CullingStateManager.INSTANCED_ENTITY_CULLING_SHADER);
        }

        if (Config.getCullChunk() && CullingStateManager.CHUNK_CULLING_MAP != null && CullingStateManager.CHUNK_CULLING_MAP.needTransferData()) {
            CullingStateManager.useShader(CullingStateManager.CHUNK_CULLING_SHADER);
            CullingStateManager.CHUNK_CULLING_MAP_TARGET.clear(Minecraft.ON_OSX);
            CullingStateManager.CHUNK_CULLING_MAP_TARGET.bindWrite(false);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f, 1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(-1.0f, 1.0f, 0.0f).endVertex();
            tessellator.end();
        }

        CullingStateManager.bindMainFrameTarget();
    }

    public void addString(List<String> list, String text) {
        list.add(text);
    }

    public void renderText(PoseStack stack, List<String> list, int width, int height) {
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < list.size(); ++i) {
            String text = list.get(i);
            font.drawShadow(stack, text, (int) (width - (font.width(text) / 2f)), height + font.lineHeight * i, 16777215);
        }
    }

    public static void setUniform(ShaderInstance shader) {
        ICullingShader shaderInstance = (ICullingShader) shader;
        if (shaderInstance.getCullingCameraPos() != null) {
            Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            float[] array = new float[]{(float) pos.x, (float) pos.y, (float) pos.z};
            shaderInstance.getCullingCameraPos().set(array);
        }
        if (shaderInstance.getCullingCameraDir() != null) {
            Vector3f pos = Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector();
            float[] array = new float[]{pos.x(), pos.y(), pos.z()};
            shaderInstance.getCullingCameraDir().set(array);
        }
        if (shaderInstance.getBoxScale() != null) {
            shaderInstance.getBoxScale().set(8.0f);
        }
        if (shaderInstance.getFrustumPos() != null && CullingStateManager.FRUSTUM != null) {
            Vec3 pos = new Vec3(
                    ((AccessorFrustum) CullingStateManager.FRUSTUM).camX(),
                    ((AccessorFrustum) CullingStateManager.FRUSTUM).camY(),
                    ((AccessorFrustum) CullingStateManager.FRUSTUM).camZ());

            float[] array = new float[]{(float) pos.x, (float) pos.y, (float) pos.z};
            shaderInstance.getFrustumPos().set(array);
        }
        if (shaderInstance.getCullingViewMat() != null) {
            shaderInstance.getCullingViewMat().set(CullingStateManager.VIEW_MATRIX);
        }
        if (shaderInstance.getCullingProjMat() != null) {
            shaderInstance.getCullingProjMat().set(CullingStateManager.PROJECTION_MATRIX);
        }
        if (shaderInstance.getCullingFrustum() != null) {
            Vector4f[] frustumData = ((AccessorFrustum) CullingStateManager.FRUSTUM).frustumData();
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
        if (shaderInstance.getRenderDistance() != null) {
            float distance = Minecraft.getInstance().options.getEffectiveRenderDistance();
            if (shader == CullingStateManager.COPY_DEPTH_SHADER) {
                if (CullingStateManager.DEPTH_INDEX > 0)
                    distance = 2;
                else
                    distance = 0;
            }

            shaderInstance.getRenderDistance().set(distance);
        }
        if (shaderInstance.getDepthSize() != null) {
            float[] array = new float[CullingStateManager.DEPTH_SIZE * 2];
            if (shader == CullingStateManager.COPY_DEPTH_SHADER) {
                array[0] = (float) CullingStateManager.DEPTH_BUFFER_TARGET[CullingStateManager.DEPTH_INDEX].width;
                array[1] = (float) CullingStateManager.DEPTH_BUFFER_TARGET[CullingStateManager.DEPTH_INDEX].height;
            } else {
                for (int i = 0; i < CullingStateManager.DEPTH_SIZE; ++i) {
                    int arrayIdx = i * 2;
                    array[arrayIdx] = (float) CullingStateManager.DEPTH_BUFFER_TARGET[i].width;
                    array[arrayIdx + 1] = (float) CullingStateManager.DEPTH_BUFFER_TARGET[i].height;
                }
            }
            shaderInstance.getDepthSize().set(array);
        }
        if (shader == CullingStateManager.COPY_DEPTH_SHADER && CullingStateManager.DEPTH_INDEX > 0 && shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float) CullingStateManager.DEPTH_BUFFER_TARGET[CullingStateManager.DEPTH_INDEX - 1].width, (float) CullingStateManager.DEPTH_BUFFER_TARGET[CullingStateManager.DEPTH_INDEX - 1].height);
        }
        if (shaderInstance.getCullingSize() != null) {
            shaderInstance.getCullingSize().set((float) CullingStateManager.CHUNK_CULLING_MAP_TARGET.width, (float) CullingStateManager.CHUNK_CULLING_MAP_TARGET.height);
        }
        if (shaderInstance.getEntityCullingSize() != null) {
            shaderInstance.getEntityCullingSize().set((float) CullingStateManager.ENTITY_CULLING_MAP_TARGET.width, (float) CullingStateManager.ENTITY_CULLING_MAP_TARGET.height);
        }
        if (shaderInstance.getLevelHeightOffset() != null) {
            shaderInstance.getLevelHeightOffset().set(CullingStateManager.LEVEL_SECTION_RANGE);
        }
        if (shaderInstance.getLevelMinSection() != null && Minecraft.getInstance().level != null) {
            int min = Minecraft.getInstance().level.getMinSection();
            shaderInstance.getLevelMinSection().set(min);
        }
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.PostLayer event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (event.getOverlay() == ForgeIngameGui.HELMET_ELEMENT) {
            partialTick = event.getPartialTicks();
        } else {
            return;
        }

        if (CullingStateManager.DEBUG > 0) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = minecraft.getWindow().getGuiScaledWidth() / 2;
            int height = 20;
            int widthScale = 80;

            List<String> monitorTexts = new ArrayList<>();

            if (CullingStateManager.fps == 0) {
                CullingStateManager.fps++;
            }

            if (CullingStateManager.cullingInitCount == 0) {
                CullingStateManager.cullingInitCount++;
            }

            int index = Minecraft.getInstance().fpsString.indexOf("fps");
            if (index != -1) {
                String extractedString = Minecraft.getInstance().fpsString.substring(0, index + 3);
                String fps = "FPS: " + extractedString;
                addString(monitorTexts, fps);
            }

            String cull = ComponentUtil.translatable("brute_force_rendering_culling.cull_entity").getString() + ": "
                    + (Config.getCullEntity() ? ComponentUtil.translatable("brute_force_rendering_culling.enable").getString() : ComponentUtil.translatable("brute_force_rendering_culling.disable").getString());
            addString(monitorTexts, cull);

            String cull_block_entity = ComponentUtil.translatable("brute_force_rendering_culling.cull_block_entity").getString() + ": "
                    + (Config.getCullBlockEntity() ? ComponentUtil.translatable("brute_force_rendering_culling.enable").getString() : ComponentUtil.translatable("brute_force_rendering_culling.disable").getString());
            addString(monitorTexts, cull_block_entity);

            String cull_chunk = ComponentUtil.translatable("brute_force_rendering_culling.cull_chunk").getString() + ": "
                    + (Config.getCullChunk() ? ComponentUtil.translatable("brute_force_rendering_culling.enable").getString() : ComponentUtil.translatable("brute_force_rendering_culling.disable").getString());
            addString(monitorTexts, cull_chunk);

            if (CullingStateManager.DEBUG > 1) {
                String Sampler = ComponentUtil.translatable("brute_force_rendering_culling.sampler").getString() + ": " + String.valueOf((Float.parseFloat(String.format("%.0f", Config.getSampling() * 100.0D))) + "%");
                addString(monitorTexts, Sampler);

                if (Config.doEntityCulling()) {
                    String blockCullingTime = ComponentUtil.translatable("brute_force_rendering_culling.block_culling_time").getString() + ": " + (CullingStateManager.blockCullingTime / 1000 / CullingStateManager.fps) + " μs";
                    addString(monitorTexts, blockCullingTime);

                    String blockCulling = ComponentUtil.translatable("brute_force_rendering_culling.block_culling").getString() + ": " + CullingStateManager.blockCulling + " / " + CullingStateManager.blockCount;
                    addString(monitorTexts, blockCulling);

                    String entityCullingTime = ComponentUtil.translatable("brute_force_rendering_culling.entity_culling_time").getString() + ": " + (CullingStateManager.entityCullingTime / 1000 / CullingStateManager.fps) + " μs";
                    addString(monitorTexts, entityCullingTime);

                    String entityCulling = ComponentUtil.translatable("brute_force_rendering_culling.entity_culling").getString() + ": " + CullingStateManager.entityCulling + " / " + CullingStateManager.entityCount;
                    addString(monitorTexts, entityCulling);

                    String initTime = ComponentUtil.translatable("brute_force_rendering_culling.entity_culling_init").getString() + ": " + (CullingStateManager.entityCullingInitTime / CullingStateManager.cullingInitCount / CullingStateManager.fps) + " ns";
                    addString(monitorTexts, initTime);
                }

                if (Config.getCullChunk()) {
                    if (CullingStateManager.CHUNK_CULLING_MAP != null) {
                        String chunkCullingCount = ComponentUtil.translatable("brute_force_rendering_culling.chunk_update_count").getString() + ": " + CullingStateManager.CHUNK_CULLING_MAP.lastQueueUpdateCount;
                        addString(monitorTexts, chunkCullingCount);
                    }

                    String cullingInitTime = ComponentUtil.translatable("brute_force_rendering_culling.chunk_culling_init").getString() + ": " + (CullingStateManager.chunkCullingInitTime / CullingStateManager.cullingInitCount / CullingStateManager.fps) + " ns";
                    addString(monitorTexts, cullingInitTime);
                }
            }

            int heightOffset = minecraft.font.lineHeight * monitorTexts.size();
            int top = height;
            int bottom = height + heightOffset;
            int left = width + widthScale;
            int right = width - widthScale;

            float bgColor = 1.0f;
            float bgAlpha = 0.3f;
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            bufferbuilder.vertex(right - 1, bottom + 1, 0.0D)
                    .color(bgColor, bgColor, bgColor, bgAlpha)
                    .uv(u(right - 1), v(bottom + 1)).endVertex();
            bufferbuilder.vertex(left + 1, bottom + 1, 0.0D)
                    .color(bgColor, bgColor, bgColor, bgAlpha)
                    .uv(u(left + 1), v(bottom + 1)).endVertex();
            bufferbuilder.vertex(left + 1, top - 1, 0.0D)
                    .color(bgColor, bgColor, bgColor, bgAlpha)
                    .uv(u(left + 1), v(top - 1)).endVertex();
            bufferbuilder.vertex(right - 1, top - 1, 0.0D)
                    .color(bgColor, bgColor, bgColor, bgAlpha)
                    .uv(u(right - 1), v(top - 1)).endVertex();
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
            CullingStateManager.useShader(CullingStateManager.REMOVE_COLOR_SHADER);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1f);
            RenderSystem.disableBlend();
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().translate(0, 0, -1);
            RenderSystem.applyModelViewMatrix();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(right, bottom, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(left, bottom, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(left, top, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(right, top, 0.0D).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            renderText(event.getMatrixStack(), monitorTexts, width, top);

            if (!CullingStateManager.checkTexture)
                return;

            Tesselator tessellator = Tesselator.getInstance();
            float screenScale = 1.0f;
            double windowScale = 0.4;
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            for (int i = 0; i < CullingStateManager.DEPTH_BUFFER_TARGET.length; ++i) {
                int scaledHeight = (int) (minecraft.getWindow().getGuiScaledHeight() * windowScale * screenScale);
                int scaledWidth = (int) (minecraft.getWindow().getGuiScaledWidth() * windowScale * screenScale);
                int offsetHeight = (int) ((1 - screenScale) * 2 * minecraft.getWindow().getGuiScaledHeight() * windowScale);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight() - offsetHeight, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(scaledWidth, minecraft.getWindow().getGuiScaledHeight() - offsetHeight, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(scaledWidth, minecraft.getWindow().getGuiScaledHeight() - scaledHeight - offsetHeight, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(0.0D, minecraft.getWindow().getGuiScaledHeight() - scaledHeight - offsetHeight, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingStateManager.DEPTH_TEXTURE[i]);
                tessellator.end();
                screenScale *= 0.5f;
            }

            if (Config.doEntityCulling()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight() * 0.25f);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth() - height, height, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double) minecraft.getWindow().getGuiScaledWidth(), height, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double) minecraft.getWindow().getGuiScaledWidth(), 0, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth() - height, 0, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingStateManager.ENTITY_CULLING_MAP_TARGET.getColorTextureId());
                tessellator.end();
            }

            if (Config.getCullChunk()) {
                height = (int) (minecraft.getWindow().getGuiScaledHeight() * 0.25f);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth() - height, height * 2, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double) minecraft.getWindow().getGuiScaledWidth(), height * 2, 0.0D).uv(1, 0.0F).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex((double) minecraft.getWindow().getGuiScaledWidth(), height, 0.0D).uv(1, 1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.vertex(minecraft.getWindow().getGuiScaledWidth() - height, height, 0.0D).uv(0.0F, 1).color(255, 255, 255, 255).endVertex();
                RenderSystem.setShaderTexture(0, CullingStateManager.CHUNK_CULLING_MAP_TARGET.getColorTextureId());
                tessellator.end();
            }
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }
}

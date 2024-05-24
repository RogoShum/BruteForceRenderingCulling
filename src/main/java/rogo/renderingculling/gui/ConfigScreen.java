package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.ComponentUtil;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.ModLoader;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {
    private boolean release = false;
    int heightScale;

    public ConfigScreen(Component titleIn) {
        super(titleIn);
        heightScale = (int) (Minecraft.getInstance().font.lineHeight * 2f + 1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static float u(int width) {
        return (float) width / Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static float v(int height) {
        return 1.0f - ((float) height / (Minecraft.getInstance().getWindow().getGuiScaledHeight()));
    }

    @Override
    public void renderBackground(PoseStack p_96557_) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth() / 2;
        int widthScale = 85;
        int right = width - widthScale;
        int left = width + widthScale;
        int bottom = (int) (minecraft.getWindow().getGuiScaledHeight() * 0.8) + 20;
        int top = bottom - heightScale * children().size() - 10;

        float bgColor = 1.0f;
        float bgAlpha = 0.3f;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1f);
        CullingStateManager.useShader(CullingStateManager.REMOVE_COLOR_SHADER);
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
        BufferUploader.drawWithShader(bufferbuilder.end());
        bgAlpha = 1.0f;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(right, bottom, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(left, bottom, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(left, top, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(right, top, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if (this.minecraft.options.keyInventory.matches(p_96552_, p_96553_)) {
            this.onClose();
            return true;
        } else if (this.minecraft.options.keyPlayerList.matches(p_96552_, p_96553_)) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(p_96552_, p_96553_, p_96554_);
        }
    }

    @Override
    public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
        if (ModLoader.CONFIG_KEY.matches(p_94715_, p_94716_)) {
            if (release) {
                this.onClose();
                return true;
            } else {
                release = true;
            }
        }
        return super.keyReleased(p_94715_, p_94716_, p_94717_);
    }

    @Override
    protected void init() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            onClose();
            return;
        }

        if (player.getName().getString().equals("Dev")) {
            addConfigButton(() -> CullingStateManager.checkCulling, (b) -> CullingStateManager.checkCulling = b, () -> ComponentUtil.literal("Debug"))
                    .setDetailMessage(() -> ComponentUtil.translatable("brute_force_rendering_culling.detail.debug"));

            addConfigButton(() -> CullingStateManager.checkTexture, (b) -> CullingStateManager.checkTexture = b, () -> ComponentUtil.literal("Check Texture"))
                    .setDetailMessage(() -> ComponentUtil.translatable("brute_force_rendering_culling.detail.check_texture"));
        }

        addConfigButton(Config::getSampling, (value) -> {
            double format = Mth.floor(value * 20) * 0.05;
            format = Double.parseDouble(String.format("%.2f", format));
            Config.setSampling(format);
            return format;
        }, (value) -> String.valueOf(Mth.floor(value * 100) + "%"), () -> ComponentUtil.translatable("brute_force_rendering_culling.sampler"))
                .setDetailMessage(() -> ComponentUtil.translatable("brute_force_rendering_culling.detail.sampler"));

        addConfigButton(() -> Config.getDepthUpdateDelay() / 10d, (value) -> {
            int format = Mth.floor(value * 10);
            if (format > 0) {
                format -= Config.getShaderDynamicDelay();
            }
            Config.setDepthUpdateDelay(format);
            format += Config.getShaderDynamicDelay();
            return format * 0.1;
        }, (value) -> {
            int format = Mth.floor(value * 10);
            return String.valueOf(format);
        }, () -> ComponentUtil.translatable("brute_force_rendering_culling.culling_map_update_delay"))
                .setDetailMessage(() -> ComponentUtil.translatable("brute_force_rendering_culling.detail.culling_map_update_delay"));

        addConfigButton(() -> Config.getCullChunk() && ModLoader.hasSodium() && !ModLoader.hasNvidium(), Config::getAsyncChunkRebuild, Config::setAsyncChunkRebuild, () -> ComponentUtil.translatable("brute_force_rendering_culling.async"))
                .setDetailMessage(() -> {
                    if (ModLoader.hasNvidium()) {
                        return ComponentUtil.translatable("brute_force_rendering_culling.detail.nvidium");
                    } else if (!ModLoader.hasSodium()) {
                        return ComponentUtil.translatable("brute_force_rendering_culling.detail.sodium");
                    } else
                        return ComponentUtil.translatable("brute_force_rendering_culling.detail.async");
                });

        addConfigButton(Config::getCullChunk, Config::setCullChunk, () -> ComponentUtil.translatable("brute_force_rendering_culling.cull_chunk"))
                .setDetailMessage(() -> ComponentUtil.translatable("brute_force_rendering_culling.detail.cull_chunk"));
        addConfigButton(Config::getCullEntity, Config::setCullEntity, () -> ComponentUtil.translatable("brute_force_rendering_culling.cull_entity"))
                .setDetailMessage(() -> {
                    if (CullingStateManager.gl33()) {
                        return ComponentUtil.translatable("brute_force_rendering_culling.detail.cull_entity");
                    } else {
                        return ComponentUtil.translatable("brute_force_rendering_culling.detail.gl33");
                    }
                });

        super.init();
    }

    public NeatButton addConfigButton(Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> updateMessage) {
        int width = 150;
        int x = this.width / 2 - width / 2;
        NeatButton button = new NeatButton(x, (int) ((height * 0.8) - heightScale * children().size()), width, 14
                , getter, setter, updateMessage);
        this.addWidget(button);
        return button;
    }

    public NeatButton addConfigButton(Supplier<Boolean> enable, Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> updateMessage) {
        int width = 150;
        int x = this.width / 2 - width / 2;
        NeatButton button = new NeatButton(x, (int) ((height * 0.8) - heightScale * children().size()), width, 14
                , enable, getter, setter, updateMessage);
        this.addWidget(button);
        return button;
    }

    public NeatSliderButton addConfigButton(Supplier<Double> getter, Function<Double, Double> setter, Function<Double, String> display, Supplier<MutableComponent> name) {
        int width = 150;
        int x = this.width / 2 - width / 2;
        NeatSliderButton button = new NeatSliderButton(x, (int) ((height * 0.8) - heightScale * children().size()), width, 14
                , getter, setter, display, name);
        this.addWidget(button);
        return button;
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        List<? extends GuiEventListener> children = children();

        for (GuiEventListener button : children) {
            if (button instanceof AbstractWidget b)
                b.render(poseStack, mouseX, mouseY, partialTicks);
        }

        for (GuiEventListener button : children) {
            Component details = null;
            if (button instanceof NeatButton b) {
                details = b.getDetails();
            }
            if (button instanceof NeatSliderButton b) {
                details = b.getDetails();
            }
            if (details != null) {
                renderButtonDetails(poseStack, details);
            }
        }
    }

    private void renderButtonDetails(PoseStack poseStack, Component details) {
        String[] parts = details.getString().split("\\n");
        int partHeight = 0;
        int textWidth = Math.min(minecraft.getWindow().getScreenWidth(), 200);
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 - (textWidth) / 2 + 2;
        for (String part : parts) {
            part = part.replace("warn:", "");
            List<FormattedCharSequence> text = Minecraft.getInstance().font.split(ComponentUtil.literal(part), textWidth);
            partHeight += text.size() * minecraft.font.lineHeight + minecraft.font.lineHeight / 2;
        }

        int width = minecraft.getWindow().getGuiScaledWidth() / 2;
        int widthScale = textWidth / 2 + 4;
        int right = width - widthScale;
        int left = width + widthScale;
        int bottom = partHeight + 2;
        int top = 2;

        float bgColor = 0.0f;
        float bgAlpha = 0.7f;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(right, bottom, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(left, bottom, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(left, top, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        bufferbuilder.vertex(right, top, 0.0D)
                .color(bgColor, bgColor, bgColor, bgAlpha).endVertex();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();

        partHeight = 0;
        for (String part : parts) {
            boolean red = part.contains("warn:");
            if (red)
                part = part.replace("warn:", "");
            List<FormattedCharSequence> text = Minecraft.getInstance().font.split(ComponentUtil.literal(part), textWidth);
            for (int row = 0; row < text.size(); ++row) {
                minecraft.font.drawShadow(poseStack, text.get(row), x, 4 + partHeight + row * minecraft.font.lineHeight,
                        red ? FastColor.ARGB32.color(255, 170, 0, 0) : FastColor.ARGB32.color(255, 255, 255, 255));
            }
            partHeight += text.size() * minecraft.font.lineHeight + minecraft.font.lineHeight / 2;
        }
    }
}

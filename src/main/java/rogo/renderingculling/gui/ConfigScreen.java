package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

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
    public void renderBackground(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth() / 2;
        int widthScale = width / 4;
        int right = width - widthScale;
        int left = width + widthScale;
        int bottom = (int) (minecraft.getWindow().getGuiScaledHeight() * 0.8) + 20;
        int top = bottom - heightScale * children().size() - 10;

        float bgColor = 1.0f;
        float bgAlpha = 0.3f;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1f);
        CullingHandler.useShader(CullingHandler.REMOVE_COLOR_SHADER);
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
        if (CullingHandler.CONFIG_KEY.matches(p_94715_, p_94716_)) {
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
            addConfigButton(() -> CullingHandler.checkCulling, (b) -> CullingHandler.checkCulling = b, () -> Component.literal("Debug"))
                    .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.debug"));

            addConfigButton(() -> CullingHandler.checkTexture, (b) -> CullingHandler.checkTexture = b, () -> Component.literal("Check Texture"))
                    .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.check_texture"));
        }

        addConfigButton(Config::getSampling, (value) -> {
            double format = Mth.floor(value * 20) * 0.05;
            format = Double.parseDouble(String.format("%.2f", format));
            Config.setSampling(format);
            return format;
        }, (value) -> String.valueOf(Mth.floor(value * 100) + "%"), () -> Component.translatable("brute_force_rendering_culling.sampler"))
                .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.sampler"));

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
        }, () -> Component.translatable("brute_force_rendering_culling.culling_map_update_delay"))
                .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.culling_map_update_delay"));

        addConfigButton(() -> Config.getCullChunk() && CullingHandler.hasSodium() && !CullingHandler.hasNvidium(), Config::getAsyncChunkRebuild, Config::setAsyncChunkRebuild, () -> Component.translatable("brute_force_rendering_culling.async"))
                .setDetailMessage(() -> {
                    if (CullingHandler.hasNvidium()) {
                        return Component.translatable("brute_force_rendering_culling.detail.nvidium").withStyle(ChatFormatting.BOLD);
                    } else if (CullingHandler.hasNvidium()) {
                        return Component.translatable("brute_force_rendering_culling.detail.sodium").withStyle(ChatFormatting.DARK_RED);
                    } else
                        return Component.translatable("brute_force_rendering_culling.detail.async");
                });
        addConfigButton(Config::getCullChunk, Config::setCullChunk, () -> Component.translatable("brute_force_rendering_culling.cull_chunk"))
                .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.cull_chunk"));
        addConfigButton(Config::getCullEntity, Config::setCullEntity, () -> Component.translatable("brute_force_rendering_culling.cull_entity"))
                .setDetailMessage(() -> Component.translatable("brute_force_rendering_culling.detail.cull_entity"));

        super.init();
    }

    public NeatButton addConfigButton(Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> updateMessage) {
        NeatButton button = new NeatButton(width / 2 - 50, (int) ((height * 0.8) - heightScale * children().size()), 100, 14
                , getter, setter, updateMessage);
        this.addWidget(button);
        return button;
    }

    public NeatButton addConfigButton(Supplier<Boolean> enable, Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> updateMessage) {
        NeatButton button = new NeatButton(width / 2 - 50, (int) ((height * 0.8) - heightScale * children().size()), 100, 14
                , enable, getter, setter, updateMessage);
        this.addWidget(button);
        return button;
    }

    public NeatSliderButton addConfigButton(Supplier<Double> getter, Function<Double, Double> setter, Function<Double, String> display, Supplier<MutableComponent> name) {
        NeatSliderButton button = new NeatSliderButton(width / 2 - 50, (int) ((height * 0.8) - heightScale * children().size()), 100, 14
                , getter, setter, display, name);
        this.addWidget(button);
        return button;
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        List<? extends GuiEventListener> children = children();

        for (GuiEventListener button : children) {
            if (button instanceof AbstractWidget b)
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        for (GuiEventListener button : children) {
            if (button instanceof NeatButton b)
                b.shouDetail(guiGraphics, minecraft.font);
            if (button instanceof NeatSliderButton b)
                b.shouDetail(guiGraphics, minecraft.font);
        }
    }
}

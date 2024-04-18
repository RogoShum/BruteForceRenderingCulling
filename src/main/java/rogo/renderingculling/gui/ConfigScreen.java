package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
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
        heightScale = (int) (Minecraft.getInstance().font.lineHeight*2f+1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth()/2;
        int widthScale = width/4;
        int bottom = (int) (minecraft.getWindow().getGuiScaledHeight()*0.8)+20;
        int top = bottom-heightScale*children().size()-10;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale, bottom, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, bottom, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, top, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width-widthScale, top, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale-2, bottom+2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, bottom+2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, top-2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width-widthScale-2, top-2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
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
        if(CullingHandler.CONFIG_KEY.matches(p_94715_, p_94716_)) {
            if(release) {
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
        if(player == null) {
            onClose();
            return;
        }

        addConfigButton(() -> CullingHandler.INSTANCE.checkCulling, (b) -> CullingHandler.INSTANCE.checkCulling = b, () -> Component.literal("Debug"));
        addConfigButton(() -> CullingHandler.INSTANCE.checkTexture, (b) -> CullingHandler.INSTANCE.checkTexture = b, () -> Component.literal("Check Texture"));

        addConfigButton(() -> Config.getCullingEntityRate()/20d, (value) -> {
            int format = Mth.floor(value * 20);
            Config.setCullingEntityRate(format);
            return format*0.05;
        }, (value) -> String.valueOf(Mth.floor(value * 20)), () -> Component.translatable("brute_force_rendering_culling.culling_entity_update_rate"));

        addConfigButton(Config::getSampling, (value) -> {
            double format = Mth.floor(value * 100)*0.01;
            format = Double.parseDouble(String.format("%.2f",format));
            Config.setSampling(format);
            return format;
        }, (value) -> String.valueOf(Mth.floor(value * 100)), () -> Component.translatable("brute_force_rendering_culling.sampler"));

        addConfigButton(() -> Config.getDepthUpdateDelay()/10d, (value) -> {
            int format = Mth.floor(value * 10);
            if(format > 0) {
                format -= Config.getShaderDynamicDelay();
            }
            Config.setDepthUpdateDelay(format);
            format += Config.getShaderDynamicDelay();
            return format * 0.1;
        }, (value) -> {
            int format = Mth.floor(value * 10);
            return String.valueOf(format);
        }, () -> Component.translatable("brute_force_rendering_culling.culling_map_update_delay"));

        Config.setCullBlock(false);
        addConfigButton(Config::getCullChunk, Config::setCullChunk, () -> Component.translatable("brute_force_rendering_culling.cull_chunk"));
        addConfigButton(Config::getCullEntity, Config::setCullEntity, () -> Component.translatable("brute_force_rendering_culling.cull_entity"));

        super.init();
    }

    public void addConfigButton(Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> updateMessage) {
        this.addWidget(new NeatButton(width/2-50, (int) ((height*0.8)-heightScale*children().size()), 100, 14
                , getter, setter, updateMessage));
    }

    public void addConfigButton(Supplier<Double> getter, Function<Double, Double> setter, Function<Double, String> display, Supplier<MutableComponent> name) {
        this.addWidget(new NeatSliderButton(width/2-50, (int) ((height*0.8)-heightScale*children().size()), 100, 14
                , getter, setter, display, name));
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        List<? extends GuiEventListener> children = children();
        for (GuiEventListener button : children) {
            if(button instanceof AbstractWidget b)
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        this.renderBackground(guiGraphics);
    }
}

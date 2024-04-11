package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.List;

public class ConfigScreen extends Screen {
    private boolean release = false;

    public ConfigScreen(Component titleIn) {
        super(titleIn);
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
        Font font = minecraft.font;
        int heightScale = font.lineHeight*8;
        int height = minecraft.getWindow().getGuiScaledHeight()/2+heightScale/2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale, height+heightScale, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, height+heightScale, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, height-heightScale, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width-widthScale, height-heightScale, -2.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale-2, height+heightScale+2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, height+heightScale+2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, height-heightScale-2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width-widthScale-2, height-heightScale-2, -1.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
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

        int heightScale = (int) (minecraft.font.lineHeight*2f)+1;
        NeatSliderButton sampler = new NeatSliderButton(width/2-50, height/2+heightScale+12, 100, 14, Config.getSampling(),
                (sliderButton) -> {
                    Component component = Component.literal((int)(sliderButton.getValue() * 100.0D) + "%");
                    return (Component.translatable("brute_force_rendering_culling.sampler")).append(": ").append(component);
                }, (value) -> {
            double v = Float.parseFloat(String.format("%.2f",value));
            Config.setSampling(v);
        });
        NeatSliderButton entityUpdateRate = new NeatSliderButton(width/2-50, height/2+heightScale*2+12, 100, 14, Config.getCullingEntityRate()/20f,
                (sliderButton) -> {
                    Component component = Component.literal( String.valueOf((int)(sliderButton.getValue() * 20.0D)));
                    return (Component.translatable("brute_force_rendering_culling.culling_entity_update_rate")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*20);
            Config.setCullingEntityRate(i);
        });
        NeatButton debug = new NeatButton(width/2-50, height/2+heightScale*4+12, 100, 14
                , (button) -> {
            CullingHandler.INSTANCE.checkCulling = !CullingHandler.INSTANCE.checkCulling;
        }, () -> (CullingHandler.INSTANCE.checkCulling ? Component.translatable("brute_force_rendering_culling.disable").append(" ").append(Component.literal("Debug"))
                : Component.translatable("brute_force_rendering_culling.enable").append(" ").append(Component.literal("Debug"))));
        NeatButton checkTexture = new NeatButton(width/2-50, height/2+heightScale*3+12, 100, 14
                , (button) -> {
            CullingHandler.INSTANCE.checkTexture = !CullingHandler.INSTANCE.checkTexture;
        }, () -> (CullingHandler.INSTANCE.checkTexture ? Component.translatable("brute_force_rendering_culling.disable").append(" ").append(Component.literal("Check Texture"))
                : Component.translatable("brute_force_rendering_culling.enable").append(" ").append(Component.literal("Check Texture"))));
        NeatSliderButton delay = new NeatSliderButton(width/2-50, height/2+12, 100, 14, Config.getDepthUpdateDelay()/10f,
                (sliderButton) -> {
                    Component component = Component.literal(String.valueOf((int)(sliderButton.getValue() * 10.0D)));
                    return (Component.translatable("brute_force_rendering_culling.culling_map_update_delay")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*10);
            Config.setDepthUpdateDelay(i);
        });
        NeatButton close = new NeatButton(width/2-50, height/2-heightScale*2+12, 100, 14 , (button) -> {
            Config.setCullEntity(!Config.getCullEntity());
        }, () -> (Config.getCullEntity() ? Component.translatable("brute_force_rendering_culling.disable").append(" ").append(Component.translatable("brute_force_rendering_culling.cull_entity"))
                : Component.translatable("brute_force_rendering_culling.enable").append(" ").append(Component.translatable("brute_force_rendering_culling.cull_entity"))));
        NeatButton chunk = new NeatButton(width/2-50, height/2-heightScale+12, 100, 14 , (button) -> {
            Config.setCullChunk(!Config.getCullChunk());
        }, () -> (Config.getCullChunk() ? Component.translatable("brute_force_rendering_culling.disable").append(" ").append(Component.translatable("brute_force_rendering_culling.cull_chunk"))
                : Component.translatable("brute_force_rendering_culling.enable").append(" ").append(Component.translatable("brute_force_rendering_culling.cull_chunk"))));
        this.addWidget(sampler);
        this.addWidget(delay);
        this.addWidget(entityUpdateRate);
        this.addWidget(close);
        this.addWidget(chunk);
        this.addWidget(debug);
        this.addWidget(checkTexture);
        super.init();
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

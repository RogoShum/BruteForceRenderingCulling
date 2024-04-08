package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.List;

public class ConfigScreen extends Screen {

    public ConfigScreen(Text titleIn) {
        super(titleIn);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack p_96557_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        int width = minecraft.getWindow().getScaledWidth()/2;
        int widthScale = width/4;
        TextRenderer font = minecraft.textRenderer;
        int heightScale = font.fontHeight*8;
        int height = minecraft.getWindow().getScaledHeight()/2+heightScale/2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_COLOR);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale, height+heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
        bufferbuilder.vertex(width+widthScale, height+heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
        bufferbuilder.vertex(width+widthScale, height-heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
        bufferbuilder.vertex(width-widthScale, height-heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale-2, height+heightScale+2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
        bufferbuilder.vertex(width+widthScale+2, height+heightScale+2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
        bufferbuilder.vertex(width+widthScale+2, height-heightScale-2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
        bufferbuilder.vertex(width-widthScale-2, height-heightScale-2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        if(CullingHandler.CONFIG_KEY.matchesKey(p_96552_, p_96553_)) {
            this.close();
            return true;
        }
        if (this.client.options.inventoryKey.matchesKey(p_96552_, p_96553_)) {
            this.close();
            return true;
        } else if (this.client.options.playerListKey.matchesKey(p_96552_, p_96553_)) {
            this.close();
            return true;
        } else {
            return super.keyPressed(p_96552_, p_96553_, p_96554_);
        }
    }

    @Override
    public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
        return super.keyReleased(p_94715_, p_94716_, p_94717_);
    }

    @Override
    protected void init() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) {
            close();
            return;
        }

        int heightScale = (int) (client.textRenderer.fontHeight*2f)+1;
        NeatSliderButton sampler = new NeatSliderButton(width/2-50, height/2+heightScale+12, 100, 14, Config.SAMPLING.getValue(),
                (sliderButton) -> {
                    Text component = new LiteralText((int)(sliderButton.getValue() * 100.0D) + "%");
                    return (new TranslatableText("brute_force_rendering_culling.sampler")).append(": ").append(component);
                }, (value) -> {
            double v = Float.parseFloat(String.format("%.2f",value));
            Config.SAMPLING.setValue(v);
        });
        NeatSliderButton entityUpdateRate = new NeatSliderButton(width/2-50, height/2+heightScale*2+12, 100, 14, Config.CULLING_ENTITY_RATE.getValue()/20f,
                (sliderButton) -> {
                    Text component = new LiteralText( String.valueOf((int)(sliderButton.getValue() * 20.0D)));
                    return (new TranslatableText("brute_force_rendering_culling.culling_entity_update_rate")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*20);
            Config.CULLING_ENTITY_RATE.setValue(i);
        });
        NeatButton debug = new NeatButton(width/2-50, height/2+heightScale*4+12, 100, 14
                , (button) -> {
            CullingHandler.INSTANCE.checkCulling = !CullingHandler.INSTANCE.checkCulling;
        }, () -> (CullingHandler.INSTANCE.checkCulling ? new TranslatableText("brute_force_rendering_culling.disable").append(" ").append(new LiteralText("Debug"))
                : new TranslatableText("brute_force_rendering_culling.enable").append(" ").append(new LiteralText("Debug"))));
        NeatSliderButton delay = new NeatSliderButton(width/2-50, height/2+12, 100, 14, Config.UPDATE_DELAY.getValue()/10f,
                (sliderButton) -> {
                    Text component = new LiteralText(String.valueOf((int)(sliderButton.getValue() * 10.0D)));
                    return (new TranslatableText("brute_force_rendering_culling.culling_map_update_delay")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*10);
            Config.UPDATE_DELAY.setValue(i);
        });
        NeatButton close = new NeatButton(width/2-50, height/2-heightScale*2+12, 100, 14 , (button) -> {
            Config.CULL_ENTITY.setValue(!Config.CULL_ENTITY.getValue());
        }, () -> (Config.CULL_ENTITY.getValue() ? new TranslatableText("brute_force_rendering_culling.disable").append(" ").append(new TranslatableText("brute_force_rendering_culling.cull_entity"))
                : new TranslatableText("brute_force_rendering_culling.enable").append(" ").append(new TranslatableText("brute_force_rendering_culling.cull_entity"))));
        NeatButton chunk = new NeatButton(width/2-50, height/2-heightScale+12, 100, 14 , (button) -> {
                Config.CULL_CHUNK.setValue(!Config.CULL_CHUNK.getValue());
            }, () -> (Config.CULL_CHUNK.getValue() ? new TranslatableText("brute_force_rendering_culling.disable").append(" ").append(new TranslatableText("brute_force_rendering_culling.cull_chunk"))
                    : new TranslatableText("brute_force_rendering_culling.enable").append(" ").append(new TranslatableText("brute_force_rendering_culling.cull_chunk"))));
        this.addSelectableChild(sampler);
        this.addSelectableChild(delay);
        this.addSelectableChild(entityUpdateRate);
        this.addSelectableChild(close);
        this.addSelectableChild(chunk);
        this.addSelectableChild(debug);
        super.init();
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        List<? extends Element> children = children();
        for (Element button : children) {
            if(button instanceof ClickableWidget b)
                b.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        this.renderBackground(matrixStack);
    }
}

package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Font;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.List;

public class ConfigScreen extends Screen {

    public ConfigScreen(Text titleIn) {
        super(titleIn);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack p_96557_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        int width = minecraft.getWindow().getGuiScaledWidth()/2;
        int widthScale = width/4;
        Font font = minecraft.font;
        int heightScale = font.lineHeight*8;
        int height = minecraft.getWindow().getGuiScaledHeight()/2+heightScale/2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale, height+heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, height+heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width+widthScale, height-heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.vertex(width-widthScale, height-heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(width-widthScale-2, height+heightScale+2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, height+heightScale+2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width+widthScale+2, height-heightScale-2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.vertex(width-widthScale-2, height-heightScale-2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
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
            onClose();
            return;
        }

        int heightScale = (int) (client.font.lineHeight*2f)+1;
        NeatSliderButton sampler = new NeatSliderButton(width/2-50, height/2+heightScale+12, 100, 14, Config.SAMPLING.get(),
                (sliderButton) -> {
                    Text component = new TextComponent((int)(sliderButton.getValue() * 100.0D) + "%");
                    return (new TranslatableComponent("brute_force_rendering_culling.sampler")).append(": ").append(component);
                }, (value) -> {
            double v = Float.parseFloat(String.format("%.2f",value));
            Config.SAMPLING.set(v);
            Config.SAMPLING.save();
        });
        NeatSliderButton entityUpdateRate = new NeatSliderButton(width/2-50, height/2+heightScale*2+12, 100, 14, Config.CULLING_ENTITY_RATE.get()/20f,
                (sliderButton) -> {
                    Text component = new TextComponent( String.valueOf((int)(sliderButton.getValue() * 20.0D)));
                    return (new TranslatableComponent("brute_force_rendering_culling.culling_entity_update_rate")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*20);
            Config.CULLING_ENTITY_RATE.set(i);
            Config.CULLING_ENTITY_RATE.save();
        });
        NeatButton debug = new NeatButton(width/2-50, height/2+heightScale*4+12, 100, 14
                , (button) -> {
            CullingHandler.INSTANCE.checkCulling = !CullingHandler.INSTANCE.checkCulling;
        }, () -> (CullingHandler.INSTANCE.checkCulling ? new TranslatableComponent("brute_force_rendering_culling.disable").append(" ").append(new TextComponent("Debug"))
                : new TranslatableComponent("brute_force_rendering_culling.enable").append(" ").append(new TextComponent("Debug"))));
        NeatSliderButton delay = new NeatSliderButton(width/2-50, height/2+12, 100, 14, Config.UPDATE_DELAY.get()/10f,
                (sliderButton) -> {
                    Text component = new TextComponent(String.valueOf((int)(sliderButton.getValue() * 10.0D)));
                    return (new TranslatableComponent("brute_force_rendering_culling.culling_map_update_delay")).append(": ").append(component);
                }, (value) -> {
            int i = (int) (value*10);
            Config.UPDATE_DELAY.set(i);
            Config.UPDATE_DELAY.save();
        });
        NeatButton close = new NeatButton(width/2-50, height/2-heightScale*2+12, 100, 14 , (button) -> {
            Config.CULL_ENTITY.set(!Config.CULL_ENTITY.get());
            Config.CULL_ENTITY.save();
        }, () -> (Config.CULL_ENTITY.get() ? new TranslatableComponent("brute_force_rendering_culling.disable").append(" ").append(new TranslatableComponent("brute_force_rendering_culling.cull_entity"))
                : new TranslatableComponent("brute_force_rendering_culling.enable").append(" ").append(new TranslatableComponent("brute_force_rendering_culling.cull_entity"))));
        NeatButton chunk = new NeatButton(width/2-50, height/2-heightScale+12, 100, 14 , (button) -> {
                Config.CULL_CHUNK.set(!Config.CULL_CHUNK.get());
                Config.CULL_CHUNK.save();
            }, () -> (Config.CULL_CHUNK.get() ? new TranslatableComponent("brute_force_rendering_culling.disable").append(" ").append(new TranslatableComponent("brute_force_rendering_culling.cull_chunk"))
                    : new TranslatableComponent("brute_force_rendering_culling.enable").append(" ").append(new TranslatableComponent("brute_force_rendering_culling.cull_chunk"))));
        this.addWidget(sampler);
        this.addWidget(delay);
        this.addWidget(entityUpdateRate);
        this.addWidget(close);
        this.addWidget(chunk);
        this.addWidget(debug);
        super.init();
    }

    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        List<? extends GuiEventListener> children = children();
        for (GuiEventListener button : children) {
            if(button instanceof AbstractWidget b)
                b.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        this.renderBackground(matrixStack);
    }

    private boolean isSlotSelected(AbstractWidget button, double mouseX, double mouseY) {
        return this.isPointInRegion(button.x, button.y, button.getWidth(), button.getWidth(), mouseX, mouseY);
    }

    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= (double)(x - 1) && mouseX < (double)(x + width + 1) && mouseY >= (double)(y - 1) && mouseY < (double)(y + height + 1);
    }
}

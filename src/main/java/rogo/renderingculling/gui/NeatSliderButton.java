package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;

public class NeatSliderButton extends AbstractOptionSliderButton {
    private final Function<NeatSliderButton, Component> updateMessage;
    private final Consumer<Double> applyValue;
    protected NeatSliderButton(int p_93380_, int p_93381_, int p_93382_, int p_93383_, double p_93384_, Function<NeatSliderButton, Component> updateMessage, Consumer<Double> applyValue) {
        super(Minecraft.getInstance().options, p_93380_, p_93381_, p_93382_, p_93383_, p_93384_);
        this.updateMessage = updateMessage;
        updateMessage();
        this.applyValue = applyValue;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public void updateMessage() {
        this.setMessage(updateMessage.apply(this));
    }

    @Override
    protected void applyValue() {
        applyValue.accept(this.value);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_93677_, int p_93678_, float p_93679_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = this.isHoveredOrFocused() ? 0.7f : 0.5f;
        bufferbuilder.vertex(this.getX(), this.getY()+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX()+width, this.getY()+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX()+width, this.getY(), 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX(), this.getY(), 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.defaultBlendFunc();
        bufferbuilder.vertex(this.getX()-1, this.getY()+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX()+width+1, this.getY()+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX()+width+1, this.getY()-1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX()-1, this.getY()-1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        int j = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        alpha = this.isHoveredOrFocused() ? 1.0f : 0.0f;
        bufferbuilder.vertex(this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY()+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX() + (int)(this.value * (double)(this.width - 8))+8, this.getY()+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX() + (int)(this.value * (double)(this.width - 8))+8, this.getY(), 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}

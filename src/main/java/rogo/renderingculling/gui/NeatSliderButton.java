package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Function;

public class NeatSliderButton extends OptionSliderWidget {
    private final Function<NeatSliderButton, Text> updateMessage;
    private final Consumer<Double> applyValue;
    protected NeatSliderButton(int p_93380_, int p_93381_, int p_93382_, int p_93383_, double p_93384_, Function<NeatSliderButton, Text> updateMessage, Consumer<Double> applyValue) {
        super(MinecraftClient.getInstance().options, p_93380_, p_93381_, p_93382_, p_93383_, p_93384_);
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
    protected void renderBackground(MatrixStack p_93600_, MinecraftClient p_93601_, int p_93602_, int p_93603_) {

    }

    @Override
    public void render(MatrixStack p_93676_, int p_93677_, int p_93678_, float p_93679_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        TextRenderer font = minecraft.textRenderer;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();

        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.DST_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float alpha = this.isHovered() ? 0.7f : 0.5f;
        bufferbuilder.vertex(this.x, this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x+width, this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x+width, this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x, this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.defaultBlendFunc();
        bufferbuilder.vertex(this.x-1, this.y+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x+width+1, this.y+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x+width+1, this.y-1, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x-1, this.y-1, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        int j = this.active ? 16777215 : 10526880;
        drawCenteredText(p_93676_, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        alpha = this.isHovered() ? 1.0f : 0.0f;
        bufferbuilder.vertex(this.x + (int)(this.value * (double)(this.width - 8)), this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x + (int)(this.value * (double)(this.width - 8))+8, this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x + (int)(this.value * (double)(this.width - 8))+8, this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.vertex(this.x + (int)(this.value * (double)(this.width - 8)), this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}

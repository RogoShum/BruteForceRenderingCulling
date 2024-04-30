package rogo.renderingculling.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeatSliderButton extends AbstractOptionSliderButton {
    private final Function<NeatSliderButton, Component> name;
    private final Consumer<Double> applyValue;
    private Supplier<Component> detailMessage;

    protected NeatSliderButton(int p_93380_, int p_93381_, int p_93382_, int p_93383_, Supplier<Double> getter, Function<Double, Double> setter, Function<Double, String> display, Supplier<MutableComponent> name) {
        super(Minecraft.getInstance().options, p_93380_, p_93381_, p_93382_, p_93383_, getter.get());
        this.name = (sliderButton) -> name.get().append(": ").append(Component.literal(display.apply(this.value)));
        updateMessage();
        this.applyValue = (value) -> this.value = setter.apply(value);
    }

    public double getValue() {
        return this.value;
    }

    public void setDetailMessage(Supplier<Component> detailMessage) {
        this.detailMessage = detailMessage;
    }
    @Override
    public void updateMessage() {
        this.setMessage(name.apply(this));
    }

    @Override
    protected void applyValue() {
        applyValue.accept(this.value);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_93677_, int p_93678_, float p_93679_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int j = this.active ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float color = this.isHovered() ? 1.0f : 0.8f;
        bufferbuilder.vertex(this.getX(), this.getY() + height, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width, this.getY() + height, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width, this.getY(), 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX(), this.getY(), 0.0D).color(color, color, color, 1.0f).endVertex();

        color = 0.7f;
        bufferbuilder.vertex(this.getX() - 1, this.getY() + height + 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width + 1, this.getY() + height + 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width + 1, this.getY() - 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() - 1, this.getY() - 1, 0.0D).color(color, color, color, 1.0f).endVertex();

        color = 1.0f;
        bufferbuilder.vertex(this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() + height, 90.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + (int) (this.value * (double) (this.width - 8)) + 8, this.getY() + height, 90.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + (int) (this.value * (double) (this.width - 8)) + 8, this.getY(), 90.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 90.0D).color(color, color, color, 1.0f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public static String wrapText(String text, int maxWidth) {
        StringBuilder result = new StringBuilder();
        int currentWidth = 0;
        String[] words = text.split("\\s+");

        for (String word : words) {
            int wordWidth = Minecraft.getInstance().font.width(word);

            // 检查当前行是否能容纳下这个单词
            if (currentWidth + wordWidth <= maxWidth) {
                result.append(word).append(" ");
                currentWidth += wordWidth + 1; // 考虑单词间的空格
            } else {
                result.append("\n").append(word).append(" ");
                currentWidth = wordWidth + 1;
            }
        }

        return result.toString();
    }

    public List<Component> getDetails() {
        if (detailMessage != null && isHovered) {
            List<Component> components = new ArrayList<>();
            String[] parts = detailMessage.get().getString().split("\\n");
            for (String part : parts) {
                Component text = Component.literal(part);
                if(part.contains("warn:")) {
                    String[] warn = part.split("warn:");
                    text = Component.literal(warn[1]).withStyle(ChatFormatting.DARK_RED);
                }
                components.add(text);
            }
            return components;
        }

        return Lists.newArrayList();
    }

    @Override
    public void onRelease(double p_93609_, double p_93610_) {
        super.onRelease(p_93609_, p_93610_);
        this.setFocused(false);
    }
}

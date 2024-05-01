package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeatButton extends Button {
    public boolean cache = false;
    public Supplier<Boolean> getter;
    public Supplier<Component> name;
    private Supplier<Boolean> enable = () -> true;
    private Supplier<Component> detailMessage;

    public NeatButton(int p_93721_, int p_93722_, int p_93723_, int p_93724_, Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> name) {
        super(p_93721_, p_93722_, p_93723_, p_93724_, name.get(), (b) -> ((NeatButton) b).updateValue(setter), DEFAULT_NARRATION);
        this.getter = getter;
        this.name = name;
    }

    public NeatButton(int p_93721_, int p_93722_, int p_93723_, int p_93724_, Supplier<Boolean> shouldEnable, Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> name) {
        this(p_93721_, p_93722_, p_93723_, p_93724_, getter, setter, name);
        this.enable = shouldEnable;
    }

    private void updateValue(Consumer<Boolean> setter) {
        if (enable.get()) {
            setter.accept(!getter.get());
        }
    }

    public void setDetailMessage(Supplier<Component> detailMessage) {
        this.detailMessage = detailMessage;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_93747_, int p_93748_, float p_93749_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        boolean display = getter.get();
        int j = display && enable.get() ? 16777215 : 10526880;
        guiGraphics.drawCenteredString(font, display ? Component.literal("■") : Component.literal("□"), this.getX() + this.width / 2 - ((this.width - 20) / 2), this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);

        if (display != cache) {
            cache = display;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float color = display ? this.isHovered() ? 1.0f : 0.8f : this.isHovered() ? 0.7f : 0.5f;
        if (!enable.get()) {
            color = 0.2f;
        }
        bufferbuilder.vertex(this.getX(), this.getY() + height, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width, this.getY() + height, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width, this.getY(), 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX(), this.getY(), 0.0D).color(color, color, color, 1.0f).endVertex();
        if (enable.get()) {
            color = 0.7f;
        }
        bufferbuilder.vertex(this.getX() - 1, this.getY() + height + 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width + 1, this.getY() + height + 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() + width + 1, this.getY() - 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        bufferbuilder.vertex(this.getX() - 1, this.getY() - 1, 0.0D).color(color, color, color, 1.0f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public Component getDetails() {
        if (isHovered) {
            return detailMessage.get();
        }
        return null;
    }

    @Override
    public void onRelease(double p_93609_, double p_93610_) {
        super.onRelease(p_93609_, p_93610_);
        this.setFocused(false);
    }
}

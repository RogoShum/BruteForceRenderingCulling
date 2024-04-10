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

import java.util.function.Supplier;

public class NeatButton extends Button {
    public NeatButton(int p_93721_, int p_93722_, int p_93723_, int p_93724_, OnPress p_93726_, Supplier<Component> updateMessage) {
        super(p_93721_, p_93722_, p_93723_, p_93724_, updateMessage.get(), (b) -> {
            p_93726_.onPress(b);
            b.setMessage(updateMessage.get());
        }, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_93747_, int p_93748_, float p_93749_) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        RenderSystem.defaultBlendFunc();
    }
}

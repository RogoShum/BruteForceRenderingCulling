package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;


public class NeatButton extends ButtonWidget {
    public NeatButton(int p_93721_, int p_93722_, int p_93723_, int p_93724_, PressAction p_93726_, Supplier<Text> updateMessage) {
        super(p_93721_, p_93722_, p_93723_, p_93724_, updateMessage.get(), (b) -> {
            p_93726_.onPress(b);
            b.setMessage(updateMessage.get());
        });
    }

    @Override
    public void renderButton(MatrixStack p_93746_, int p_93747_, int p_93748_, float p_93749_) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        TextRenderer font = minecraft.textRenderer;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        //RenderSystem.disableDepthTest();
        drawCenteredText(p_93746_, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    protected void renderBackground(MatrixStack p_93661_, MinecraftClient p_93662_, int p_93663_, int p_93664_) {
        super.renderBackground(p_93661_, p_93662_, p_93663_, p_93664_);
    }
}

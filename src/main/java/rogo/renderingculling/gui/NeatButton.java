package rogo.renderingculling.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;


public class NeatButton extends Button {
    public NeatButton(int p_93721_, int p_93722_, int p_93723_, int p_93724_, OnPress p_93726_, Supplier<Component> updateMessage) {
        super(p_93721_, p_93722_, p_93723_, p_93724_, updateMessage.get(), (b) -> {
            p_93726_.onPress(b);
            b.setMessage(updateMessage.get());
        });
    }

    @Override
    public void renderButton(PoseStack p_93746_, int p_93747_, int p_93748_, float p_93749_) {
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
        bufferbuilder.vertex(this.x, this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x+width, this.y+height, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x+width, this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x, this.y, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.defaultBlendFunc();
        bufferbuilder.vertex(this.x-1, this.y+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x+width+1, this.y+height+1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x+width+1, this.y-1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.vertex(this.x-1, this.y-1, 90.0D).color(alpha, alpha, alpha, 0.5f).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        int j = getFGColor();
        //RenderSystem.disableDepthTest();
        drawCenteredString(p_93746_, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    protected void renderBg(PoseStack p_93661_, Minecraft p_93662_, int p_93663_, int p_93664_) {
        super.renderBg(p_93661_, p_93662_, p_93663_, p_93664_);
    }
}

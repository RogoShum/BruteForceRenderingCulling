package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rogo.renderingculling.api.IRenderSectionVisibility;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public abstract class MixinRenderChunk implements IRenderSectionVisibility {

    @Shadow @Final private BlockPos.MutableBlockPos origin;
    @Unique
    private int cullingLastVisibleTick;

    @Override
    public boolean shouldCheckVisibility(int clientTick) {
        return clientTick - cullingLastVisibleTick > 20;
    }

    @Override
    public void updateVisibleTick(int clientTick) {
        cullingLastVisibleTick = clientTick;
    }

    @Override
    public int getPositionX() {
        return origin.getX();
    }

    @Override
    public int getPositionY() {
        return origin.getY();
    }

    @Override
    public int getPositionZ() {
        return origin.getZ();
    }
}

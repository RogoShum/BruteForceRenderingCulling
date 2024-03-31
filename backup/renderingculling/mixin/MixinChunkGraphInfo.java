package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphInfo;
import org.spongepowered.asm.mixin.Mixin;
import rogo.renderingculling.AccessorChunkGraphInfo;

@Mixin(ChunkGraphInfo.class)
public class MixinChunkGraphInfo implements AccessorChunkGraphInfo {
    private boolean visible;
    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean v) {
        visible = v;
    }
}

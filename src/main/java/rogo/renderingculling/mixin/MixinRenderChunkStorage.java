package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import rogo.renderingculling.api.impl.IRenderChunkStorage;

import java.util.LinkedHashSet;

@Mixin(LevelRenderer.RenderChunkStorage.class)
public abstract class MixinRenderChunkStorage implements IRenderChunkStorage {
    @Mutable
    @Shadow
    @Final
    public LinkedHashSet<LevelRenderer.RenderChunkInfo> renderChunks;

    @Mutable
    @Shadow
    @Final
    public LevelRenderer.RenderInfoMap renderInfoMap;

    @Override
    public void copy(LevelRenderer.RenderChunkStorage storage) {
        this.renderChunks = storage.renderChunks;
        this.renderInfoMap = storage.renderInfoMap;
    }
}

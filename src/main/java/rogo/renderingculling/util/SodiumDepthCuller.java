package rogo.renderingculling.util;

import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.Minecraft;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.DepthCuller;

public class SodiumDepthCuller extends DepthCuller<VisibleChunkCollector> {
    private OcclusionCuller occlusionCuller;
    private VisibleChunkCollector visitor;

    @Override
    public void updateVisibleChunks() {
        if (this.occlusionCuller == null) return;
        VisibleChunkCollector visitor = new VisibleChunkCollector(CullingHandler.INSTANCE.getFrame());
        Viewport viewport = ((ViewportProvider) CullingHandler.FRUSTUM).sodium$createViewport();
        this.occlusionCuller.findVisible(visitor, viewport, Minecraft.getInstance().options.getEffectiveRenderDistance(), true, CullingHandler.INSTANCE.getFrame());
        this.visitor = visitor;
    }

    @Override
    public VisibleChunkCollector getResult() {
        return visitor;
    }

    public void setOcclusionCuller(OcclusionCuller culler) {
        this.occlusionCuller = culler;
    }

    public boolean hasOcclusionCuller() {
        return this.occlusionCuller != null;
    }
}

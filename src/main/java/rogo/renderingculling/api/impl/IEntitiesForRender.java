package rogo.renderingculling.api.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

public interface IEntitiesForRender {
    ObjectArrayList<SectionRenderDispatcher.RenderSection> renderChunksInFrustum();
}

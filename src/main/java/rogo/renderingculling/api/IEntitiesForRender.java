package rogo.renderingculling.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface IEntitiesForRender {
    ObjectArrayList<?> renderChunksInFrustum();
}

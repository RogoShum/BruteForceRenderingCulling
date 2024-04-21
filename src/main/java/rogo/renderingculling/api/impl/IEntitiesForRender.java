package rogo.renderingculling.api.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface IEntitiesForRender {
    ObjectArrayList<?> renderChunksInFrustum();
}

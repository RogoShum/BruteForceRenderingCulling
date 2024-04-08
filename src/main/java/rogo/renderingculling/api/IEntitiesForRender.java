package rogo.renderingculling.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;

public interface IEntitiesForRender {
    ObjectArrayList<?> renderChunksInFrustum();
}

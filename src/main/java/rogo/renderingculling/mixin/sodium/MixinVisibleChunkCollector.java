package rogo.renderingculling.mixin.sodium;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rogo.renderingculling.api.impl.ICollectorAccessor;

@Mixin(VisibleChunkCollector.class)
public abstract class MixinVisibleChunkCollector implements ICollectorAccessor {
    @Shadow(remap = false) @Final private ObjectArrayList<ChunkRenderList> sortedRenderLists;

    @Shadow(remap = false) protected abstract void addToRebuildLists(RenderSection section);

    @Override
    public void addAsyncToRebuildLists(RenderSection renderSection) {
        this.addToRebuildLists(renderSection);
    }

    @Override
    public void addRenderList(ChunkRenderList renderList) {
        this.sortedRenderLists.add(renderList);
    }
}

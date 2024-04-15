package rogo.renderingculling.mixin;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import rogo.renderingculling.util.SodiumChunkUploader;

@Mixin(RenderSectionManager.class)
public interface InvokerRenderSectionManager {

    @Invoker(value = "getRenderSection", remap = false)
    RenderSection invokeGetRenderSection(int x, int y, int z);

    @Mixin(SodiumWorldRenderer.class)
    interface AccessorSodiumWorldRenderer {

        @Accessor(value = "renderSectionManager", remap = false)
        RenderSectionManager getRenderSectionManager();
    }
}


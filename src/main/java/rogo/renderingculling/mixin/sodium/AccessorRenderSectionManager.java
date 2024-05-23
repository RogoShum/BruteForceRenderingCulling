package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSectionManager.class)
public interface AccessorRenderSectionManager {

    @Accessor(value = "fogRenderCutoff", remap = false)
    double invokeFogRenderCutoff();

    @Accessor(value = "useFogCulling", remap = false)
    boolean invokeUseFogCulling();

    @Accessor(value = "useOcclusionCulling", remap = false)
    boolean invokeUseOcclusionCulling();
}


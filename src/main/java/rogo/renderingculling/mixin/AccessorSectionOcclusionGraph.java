package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionOcclusionGraph.class)
public interface AccessorSectionOcclusionGraph {

    @Accessor("needsFullUpdate")
    boolean getNeedsFullUpdate();
}


package rogo.renderingculling.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface AccessorFrustum {
    @Accessor("camX")
    double camX();

    @Accessor("camY")
    double camY();

    @Accessor("camZ")
    double camZ();

    @Accessor("intersection")
    FrustumIntersection frustumIntersection();
}
package rogo.renderingculling.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface AccessorFrustum {
    @Accessor("x")
    double camX();

    @Accessor("y")
    double camY();

    @Accessor("z")
    double camZ();

    @Accessor("homogeneousCoordinates")
    Vector4f[] frustumData();
}
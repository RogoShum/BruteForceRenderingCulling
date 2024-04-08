package rogo.renderingculling.mixin;

import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.culling.Frustum;
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

    @Accessor("frustumData")
    Vector4f[] frustumData();
}
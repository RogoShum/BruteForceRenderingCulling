package rogo.renderingculling.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class MixinLevel {

    @Inject(method = "guardEntityTick", at = @At(value = "HEAD"), cancellable = true)
    public <T extends Entity> void onEntityTick(Consumer<T> p_46654_, T entity, CallbackInfo ci) {
        if (!Config.getCullEntity() || (entity.level() instanceof ServerLevel)) return;
        AABB aabb = entity.getBoundingBoxForCulling().inflate(0.5D);
        if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
            aabb = new AABB(entity.getX() - 2.0D, entity.getY() - 2.0D, entity.getZ() - 2.0D, entity.getX() + 2.0D, entity.getY() + 2.0D, entity.getZ() + 2.0D);
        }
        if (CullingHandler.FRUSTUM != null && !CullingHandler.FRUSTUM.isVisible(aabb)) {
            if (entity.tickCount % (20 - Config.getCullingEntityRate() + 1) != 0) {
                entity.tickCount++;
                ci.cancel();
            }
        } else if (CullingHandler.culledEntity.contains(entity)) {
            if (entity.tickCount % (20 - Config.getCullingEntityRate() + 1) != 0) {
                entity.tickCount++;
                ci.cancel();
            }
        }
    }
}

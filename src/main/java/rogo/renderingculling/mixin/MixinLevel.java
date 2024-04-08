package rogo.renderingculling.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class MixinLevel {

    @Inject(method = "tickEntity", at=@At(value = "HEAD"), cancellable = true)
    public <T extends Entity> void onEntityTick(Consumer<T> p_46654_, T entity, CallbackInfo ci) {
        if(!Config.CULL_ENTITY.getValue() || (entity.world instanceof ServerWorld)) return;
        Box aabb = entity.getVisibilityBoundingBox().expand(0.5D);
        if (aabb.isValid() || aabb.getAverageSideLength() == 0.0D) {
            aabb = new Box(entity.getX() - 2.0D, entity.getY() - 2.0D, entity.getZ() - 2.0D, entity.getX() + 2.0D, entity.getY() + 2.0D, entity.getZ() + 2.0D);
        }
        if(CullingHandler.FRUSTUM != null && !CullingHandler.FRUSTUM.isVisible(aabb)) {
            if(entity.age % (20-Config.CULLING_ENTITY_RATE.getValue()+1) != 0) {
                entity.age++;
                ci.cancel();
            }
        } else if(CullingHandler.INSTANCE.culledEntity.contains(entity)) {
            if(entity.age % (20-Config.CULLING_ENTITY_RATE.getValue()+1) != 0) {
                entity.age++;
                ci.cancel();
            }
        }
    }
}

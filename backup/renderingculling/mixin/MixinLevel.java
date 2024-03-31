package rogo.renderingculling.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.Config;
import rogo.renderingculling.CullingHandler;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class MixinLevel {

    @Inject(method = "guardEntityTick", at=@At(value = "HEAD"), cancellable = true)
    public <T extends Entity> void onEntityTick(Consumer<T> p_46654_, T entity, CallbackInfo ci) {
        if(!CullingHandler.CULLING || (entity.level instanceof ServerLevel)) return;
        AABB aabb = entity.getBoundingBoxForCulling().inflate(0.5D);
        if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
            aabb = new AABB(entity.getX() - 2.0D, entity.getY() - 2.0D, entity.getZ() - 2.0D, entity.getX() + 2.0D, entity.getY() + 2.0D, entity.getZ() + 2.0D);
        }
        if(CullingHandler.FRUSTUM != null && !CullingHandler.FRUSTUM.isVisible(aabb)) {
            if(entity.tickCount % (20-Config.CULLING_ENTITY_RATE.get()+1) != 0) {
                entity.tickCount++;
                ci.cancel();
            }
        } else if(CullingHandler.INSTANCE.culledEntity.contains(entity)) {
            if(entity.tickCount % (20-Config.CULLING_ENTITY_RATE.get()+1) != 0) {
                entity.tickCount++;
                ci.cancel();
            }
        }
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V"))
    private void injected(TickingBlockEntity instance) {
        if(!CullingHandler.CULLING || ((Object)this) instanceof ServerLevel) {
            instance.tick();
            return;
        }
        AABB aabb = new AABB(instance.getPos()).inflate(0.5D);
        if(CullingHandler.FRUSTUM != null && !CullingHandler.FRUSTUM.isVisible(aabb)) {
            if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % (20-Config.CULLING_BLOCK_RATE.get()+1) == 0) {
                instance.tick();
            }
        } else if(CullingHandler.INSTANCE.culledBlock.contains(instance.getPos())) {
            if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % (20-Config.CULLING_BLOCK_RATE.get()+1) == 0) {
                instance.tick();
            }
        } else
            instance.tick();
    }
}

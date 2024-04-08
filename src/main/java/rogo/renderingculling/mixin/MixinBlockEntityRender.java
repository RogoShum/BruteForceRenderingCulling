package rogo.renderingculling.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRender {

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;runReported(Lnet/minecraft/block/entity/BlockEntity;Ljava/lang/Runnable;)V"), cancellable = true)
    public <E extends BlockEntity> void onShouldRender(E p_112268_, float p_112269_, MatrixStack p_112270_, VertexConsumerProvider p_112271_, CallbackInfo ci) {
        Box aabb = new Box(p_112268_.getPos());
        aabb.expand(Vec3d.ofCenter(p_112268_.getPos()).distanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getPos()) * 0.03125);
        if(CullingHandler.INSTANCE.shouldSkipBlock(p_112268_, aabb, p_112268_.getPos()))
            ci.cancel();
    }
}

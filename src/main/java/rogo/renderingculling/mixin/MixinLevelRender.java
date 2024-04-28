package rogo.renderingculling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IEntitiesForRender;
import rogo.renderingculling.util.VanillaAsyncUtil;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRender implements IEntitiesForRender {

    @Mutable
    @Final
    @Shadow
    private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

    @Shadow
    @Nullable
    protected abstract ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos p_109729_, ChunkRenderDispatcher.RenderChunk p_109730_, Direction p_109731_);

    @Shadow
    @Nullable
    private ViewArea viewArea;

    @Inject(method = "setupRender", at = @At(value = "HEAD"))
    public void onSetupRenderHead(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_, CallbackInfo ci) {
        if (this.viewArea != null) {
            VanillaAsyncUtil.update((LevelRenderer) (Object) this, this.viewArea.chunks.length);
        }
    }

    @Inject(method = "applyFrustum", at = @At(value = "HEAD"))
    public void onApplyFrustumHead(Frustum p_194355_, CallbackInfo ci) {
        CullingHandler.applyFrustum = true;
    }

    @Inject(method = "applyFrustum", at = @At(value = "RETURN"))
    public void onApplyFrustumReturn(Frustum p_194355_, CallbackInfo ci) {
        CullingHandler.applyFrustum = false;
    }

    @Inject(method = "prepareCullFrustum", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;<init>(Lcom/mojang/math/Matrix4f;Lcom/mojang/math/Matrix4f;)V"))
    public void onPrepareCullFrustum(PoseStack p_172962_, Vec3 p_172963_, Matrix4f p_172964_, CallbackInfo ci) {
        CullingHandler.PROJECTION_MATRIX = new Matrix4f(p_172964_);
    }

    @Override
    public ObjectArrayList<?> renderChunksInFrustum() {
        return renderChunksInFrustum;
    }

    @Override
    public ChunkRenderDispatcher.RenderChunk invokeGetRelativeFrom(BlockPos pos, ChunkRenderDispatcher.RenderChunk chunk, Direction dir) {
        return this.getRelativeFrom(pos, chunk, dir);
    }

    @Override
    public ChunkRenderDispatcher.RenderChunk invokeGetRenderChunkAt(BlockPos pos) {
        return ((AccessorViewArea) this.viewArea).invokeGetRenderChunkAt(pos);
    }

    @Mixin(ViewArea.class)
    public interface AccessorViewArea {

        @Invoker("getRenderChunkAt")
        ChunkRenderDispatcher.RenderChunk invokeGetRenderChunkAt(BlockPos pos);
    }
}


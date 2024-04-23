package rogo.renderingculling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IEntitiesForRender;
import rogo.renderingculling.api.impl.IRenderChunkInfo;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.util.VanillaAsyncUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRender implements IEntitiesForRender {

    @Final
    @Shadow
    private ObjectArrayList<?> renderChunksInFrustum;

    @Shadow @Nullable protected abstract ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos p_109729_, ChunkRenderDispatcher.RenderChunk p_109730_, Direction p_109731_);

    @Shadow @Nullable private ViewArea viewArea;

    @Shadow @Final private AtomicReference<LevelRenderer.RenderChunkStorage> renderChunkStorage;

    @Inject(method = "applyFrustum", at = @At(value = "RETURN"))
    public void onApplyFrustum(Frustum p_194355_, CallbackInfo ci) {
        if (Config.shouldCullChunk() && !Config.getAsyncChunkRebuild()) {
            if (CullingHandler.OptiFine != null) {
                try {
                    Field field = LevelRenderer.class.getDeclaredField("renderInfosTerrain");
                    field.setAccessible(true);
                    Object value = field.get(this);

                    if (value instanceof ObjectArrayList) {
                        ((ObjectArrayList<?>) value).removeIf((o -> {
                            ChunkRenderDispatcher.RenderChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                            return !CullingHandler.shouldRenderChunk((IRenderSectionVisibility) chunk, true);
                        }));
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
            }

            this.renderChunksInFrustum.removeIf((o -> {
                ChunkRenderDispatcher.RenderChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                return !CullingHandler.shouldRenderChunk((IRenderSectionVisibility) chunk, true);
            }));
        }
    }

    @Inject(method = "updateRenderChunks", at = @At(value = "INVOKE", target = "Ljava/util/Queue;isEmpty()Z"), cancellable = true)
    public void onUpdateRenderChunks(LinkedHashSet<LevelRenderer.RenderChunkInfo> p_194363_, LevelRenderer.RenderInfoMap p_194364_, Vec3 p_194365_, Queue<LevelRenderer.RenderChunkInfo> p_194366_, boolean p_194367_, CallbackInfo ci) {
        if (Config.getAsyncChunkRebuild()) {
            ci.cancel();
        }
    }

    @Inject(method = "setupRender", at = @At(value = "HEAD"))
    public void onSetupRenderHead(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_, CallbackInfo ci) {
        if(this.viewArea != null) {
            VanillaAsyncUtil.update((LevelRenderer) (Object)this, this.viewArea.chunks.length);
        }
    }

    @Inject(method = "setupRender", at = @At(value = "RETURN"))
    public void onSetupRenderEnd(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean p_194342_, CallbackInfo ci) {
        if (Config.getAsyncChunkRebuild() && VanillaAsyncUtil.getChunkStorage() != null) {
            this.renderChunkStorage.set(VanillaAsyncUtil.getChunkStorage());
        }
    }

    @Inject(method = "initializeQueueForFullUpdate", at = @At(value = "HEAD"), cancellable = true)
    public void onInitializeQueueForFullUpdate(Camera p_194344_, Queue<LevelRenderer.RenderChunkInfo> p_194345_, CallbackInfo ci) {
        if (Config.getAsyncChunkRebuild()) {
            ci.cancel();
        }
    }

    @Inject(method = "prepareCullFrustum", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;<init>(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
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
        return ((AccessorViewArea)this.viewArea).invokeGetRenderChunkAt(pos);
    }

    @Mixin(LevelRenderer.RenderChunkInfo.class)
    public interface AccessorRenderChunkInfo {

        @Accessor("directions")
        byte getDirections();

        @Accessor("step")
        int getStep();
    }

    @Mixin(ViewArea.class)
    public interface AccessorViewArea {

        @Invoker("getRenderChunkAt")
        ChunkRenderDispatcher.RenderChunk invokeGetRenderChunkAt(BlockPos pos);
    }
}


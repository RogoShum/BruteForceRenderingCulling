package rogo.renderingculling.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.*;

import java.lang.reflect.Field;

@Mixin(LevelRenderer.class)
public class MixinLevelRender implements IEntitiesForRender {

    @Final
    @Shadow
    private ObjectArrayList<?> renderChunksInFrustum;

    @Inject(method = "applyFrustum", at = @At(value = "RETURN"))
    public void onApplyFrustum(Frustum p_194355_, CallbackInfo ci) {
        if (Config.getCullChunk()) {
            if(CullingHandler.OptiFine != null) {
                try {
                    Field field = LevelRenderer.class.getDeclaredField("renderInfosTerrain");
                    field.setAccessible(true);
                    Object value = field.get(this);

                    if (value instanceof ObjectArrayList) {
                        ((ObjectArrayList<?>)value).removeIf((o -> {
                            ChunkRenderDispatcher.RenderChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                            return !CullingHandler.INSTANCE.shouldRenderChunk((IRenderSectionVisibility) chunk, true);
                        }));
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
            }

            this.renderChunksInFrustum.removeIf((o -> {
                ChunkRenderDispatcher.RenderChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                return !CullingHandler.INSTANCE.shouldRenderChunk((IRenderSectionVisibility) chunk, true);
            }));
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
}


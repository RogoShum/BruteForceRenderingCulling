package rogo.renderingculling.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IEntitiesForRender;
import rogo.renderingculling.api.IRenderChunkInfo;

import java.lang.reflect.Field;

@Mixin(WorldRenderer.class)
public class MixinLevelRender implements IEntitiesForRender {

    @Final
    @Shadow
    private ObjectArrayList<?> chunkInfos;

    @Inject(method = "applyFrustum", at = @At(value = "RETURN"))
    public void onApplyFrustum(Frustum p_194355_, CallbackInfo ci) {
        if (Config.CULL_CHUNK.getValue()) {
            if(CullingHandler.OptiFine != null) {
                try {
                    Field field = WorldRenderer.class.getDeclaredField("renderInfosTerrain");
                    field.setAccessible(true);
                    Object value = field.get(this);

                    if (value instanceof ObjectArrayList) {
                        ((ObjectArrayList<?>)value).removeIf((o -> {
                            ChunkBuilder.BuiltChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                            return !CullingHandler.INSTANCE.shouldRenderChunk(chunk.getBoundingBox());
                        }));
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
            }

            this.chunkInfos.removeIf((o -> {
                ChunkBuilder.BuiltChunk chunk = ((IRenderChunkInfo) o).getRenderChunk();
                return !CullingHandler.INSTANCE.shouldRenderChunk(chunk.getBoundingBox());
            }));
        }
    }

    @Inject(method = "setupFrustum", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Frustum;<init>(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;)V"))
    public void onSetupFrustum(MatrixStack p_172962_, Vec3d p_172963_, Matrix4f p_172964_, CallbackInfo ci) {
        CullingHandler.PROJECTION_MATRIX = p_172964_.copy();
    }

    @Override
    public ObjectArrayList<?> renderChunksInFrustum() {
        return chunkInfos;
    }
}


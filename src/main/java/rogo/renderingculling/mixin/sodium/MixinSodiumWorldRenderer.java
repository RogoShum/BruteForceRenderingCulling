package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.util.SodiumSectionAsyncUtil;

@Mixin(SodiumWorldRenderer.class)
public abstract class MixinSodiumWorldRenderer {

    @Shadow(remap = false)
    private RenderSectionManager renderSectionManager;

    @Inject(method = "updateChunks", at = @At(value = "HEAD"), remap = false)
    public void injectTerrainSetup(Camera camera, Frustum frustum, int frame, boolean spectator, CallbackInfo ci) {
        if (Config.shouldCullChunk()) {
            SodiumSectionAsyncUtil.update(camera, frustum, ((AccessorRenderSectionManager) this.renderSectionManager).invokeFogRenderCutoff()
                    , ((AccessorRenderSectionManager) this.renderSectionManager).invokeUseFogCulling()
                    , ((AccessorRenderSectionManager) this.renderSectionManager).invokeUseOcclusionCulling());
            if (SodiumSectionAsyncUtil.needSyncRebuild) {
                this.renderSectionManager.markGraphDirty();
                SodiumSectionAsyncUtil.needSyncRebuild = false;
            }
        }
    }
}
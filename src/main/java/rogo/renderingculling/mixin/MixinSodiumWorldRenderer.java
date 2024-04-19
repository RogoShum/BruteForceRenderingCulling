package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IRenderSectionVisibility;
import rogo.renderingculling.util.SodiumAsyncSectionUtil;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(SodiumWorldRenderer.class)
public abstract class MixinSodiumWorldRenderer {

    @Shadow(remap = false)
    private RenderSectionManager renderSectionManager;

    @Inject(method = "setupTerrain", at = @At(value = "HEAD"), remap = false)
    public void injectTerrainSetup(Camera camera, Viewport viewport, int frame, boolean spectator, boolean updateChunksImmediately, CallbackInfo ci) {
        if (Config.shouldCullChunk()) {
            SodiumAsyncSectionUtil.update(viewport, ((AccessorRenderSectionManager) this.renderSectionManager).invokeSearchDistance()
                    , ((AccessorRenderSectionManager) this.renderSectionManager).invokeShouldUseOcclusionCulling(camera, spectator));
        }
    }
}
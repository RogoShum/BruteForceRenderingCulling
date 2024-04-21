package rogo.renderingculling.mixin.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderSectionManager.class)
public interface AccessorRenderSectionManager {

    @Invoker(value = "getSearchDistance", remap = false)
    float invokeSearchDistance();

    @Invoker(value = "shouldUseOcclusionCulling", remap = false)
    boolean invokeShouldUseOcclusionCulling(Camera camera, boolean spectator);
}


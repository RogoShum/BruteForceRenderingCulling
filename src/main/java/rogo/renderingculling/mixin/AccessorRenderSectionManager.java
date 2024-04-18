package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayDeque;
import java.util.Map;

@Mixin(RenderSectionManager.class)
public interface AccessorRenderSectionManager {

    @Invoker(value = "getSearchDistance", remap = false)
    float invokeSearchDistance();

    @Invoker(value = "shouldUseOcclusionCulling", remap = false)
    boolean invokeShouldUseOcclusionCulling(Camera camera, boolean spectator);

    @Accessor(value = "rebuildLists", remap = false)
    Map<ChunkUpdateType, ArrayDeque<RenderSection>> accessorOutputRebuildQueue();
}


package rogo.renderingculling.util;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.VisibleChunkUploader;
import rogo.renderingculling.mixin.InvokerRenderSectionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

public class SodiumChunkUploader extends VisibleChunkUploader<VisibleChunkCollector> {
    public static volatile int frame = 0;
    public static int lastFrame = 0;

    @Override
    public void update() {
        if(lastFrame != frame) {
            lastFrame = frame;
            VisibleChunkCollector visitor = new VisibleChunkCollector(frame);
            RenderSectionManager manager = ((InvokerRenderSectionManager.AccessorSodiumWorldRenderer)SodiumWorldRenderer.instance()).getRenderSectionManager();

            List<RenderSection> sections = Lists.newArrayList();
            Queue<BlockPos> queue = CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks();
            for(BlockPos pos : queue) {
                RenderSection section = ((InvokerRenderSectionManager)manager).invokeGetRenderSection(pos.getX(), pos.getY(), pos.getZ());

                if(section != null) {
                    visitor.visit(section, true);
                    sections.add(section);
                }
            }

            CullingHandler.CHUNK_CULLING_MAP.setUploaderResult(visitor);
        }
    }

    public static VisibleChunkCollector hot(int frame, VisibleChunkCollector visitor, Function<BlockPos, RenderSection> function) {
        frame = CullingHandler.INSTANCE.getFrame();
        List<RenderSection> sections = Lists.newArrayList();
        Queue<BlockPos> queue = CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks();
        for(BlockPos pos : queue) {
            RenderSection section = function.apply(pos);

            if(section != null) {
                if (section.getLastVisibleFrame() != frame) {
                    section.setLastVisibleFrame(frame);
                }
                //visitor.visit(section, true);
                sections.add(section);
            }
        }

        return null;
    }
}
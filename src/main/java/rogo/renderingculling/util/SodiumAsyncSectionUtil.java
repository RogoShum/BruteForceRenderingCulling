package rogo.renderingculling.util;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IRenderSectionVisibility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SodiumAsyncSectionUtil {
    private static int frame = 0;
    private static OcclusionCuller occlusionCuller;
    private static Viewport viewport;
    private static float searchDistance;
    private static boolean useOcclusionCulling;
    private static final AtomicReference<List<RenderSection>> atomicBfsResult = new AtomicReference<>();
    private static Map<ChunkUpdateType, ArrayDeque<RenderSection>> outputRebuildQueue;

    public static void fromSectionManager(Long2ReferenceMap<RenderSection> sections, Level world, Map<ChunkUpdateType, ArrayDeque<RenderSection>> outputRebuildQueue) {
        SodiumAsyncSectionUtil.outputRebuildQueue = outputRebuildQueue;
        SodiumAsyncSectionUtil.occlusionCuller = new OcclusionCuller(sections, world);
    }

    public static void asyncSearchRebuildSection() {
        List<RenderSection> chunkUpdates = new ArrayList<>();
        final OcclusionCuller.Visitor visitor = (section, visible) -> {
            if (section.getPendingUpdate() != null && section.getBuildCancellationToken() == null) {
                if ((!((IRenderSectionVisibility) section).isSubmittedRebuild()) && !((IRenderSectionVisibility) section).isSearched()) {//If it is in submission queue or seen dont enqueue
                    //Set that the section has been seen
                    ((IRenderSectionVisibility) section).setSearch(true);
                    chunkUpdates.add(section);
                }
            }
        };

        try {
            occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame ++);
        } catch (Throwable e) {
            System.err.println("Error doing traversal");
            e.printStackTrace();
        }

        if (!chunkUpdates.isEmpty()) {
            var previous = atomicBfsResult.getAndSet(chunkUpdates);
            if (previous != null) {
                //We need to cleanup our state from a previous iteration
                for (var section : previous) {
                    if (section.isDisposed())
                        continue;
                    //Reset that it hasnt been seen
                    ((IRenderSectionVisibility) section).setSearch(false);
                }
            }
        }
    }

    public static void update(Viewport viewport, float searchDistance, boolean useOcclusionCulling, Map<ChunkUpdateType, ArrayDeque<RenderSection>> outputRebuildQueue) {
        SodiumAsyncSectionUtil.viewport = viewport;
        SodiumAsyncSectionUtil.searchDistance = searchDistance;
        SodiumAsyncSectionUtil.useOcclusionCulling = useOcclusionCulling;
        SodiumAsyncSectionUtil.outputRebuildQueue = outputRebuildQueue;

        var bfsResult = atomicBfsResult.getAndSet(null);
        if (bfsResult != null) {
            for (var section : bfsResult) {
                if (section.isDisposed())
                    continue;
                var type = section.getPendingUpdate();
                if (type != null && section.getBuildCancellationToken() == null) {
                    var queue = outputRebuildQueue.get(type);
                    if (queue.size() < type.getMaximumQueueSize()) {
                        ((IRenderSectionVisibility) section).setSubmittedRebuild(true);
                        queue.add(section);
                    }
                }
                //Reset that the section has not been seen (whether its been submitted to the queue or not)
                ((IRenderSectionVisibility) section).setSearch(false);
            }
        }
    }

    public static SortedRenderLists handleRenderSearch(Viewport viewport, VisibleChunkCollector visitor, Function<BlockPos, RenderSection> getRenderSection) {
        for (BlockPos pos : CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks()) {
            RenderSection section = getRenderSection.apply(pos);
            if (section != null)
                visitor.visit(section, viewport.isBoxVisible(pos.getX() << 4, pos.getY() << 4, pos.getZ() << 4, 16));
        }
        SortedRenderLists sortedRenderLists = visitor.createRenderLists();

        return sortedRenderLists;
    }
}

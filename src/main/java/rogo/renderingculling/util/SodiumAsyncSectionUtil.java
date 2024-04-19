package rogo.renderingculling.util;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IChunkRenderList;
import rogo.renderingculling.api.IRenderSectionVisibility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SodiumAsyncSectionUtil {
    private static int frame = 0;
    private static volatile int sodiumLastChunkUpdateFrame = 0;
    private static OcclusionCuller occlusionCuller;
    private static Viewport viewport;
    private static float searchDistance;
    private static boolean useOcclusionCulling;
    private static final AtomicReference<Set<RenderSection>> atomicBfsResult = new AtomicReference<>();
    private static volatile boolean shouldSearch = true;
    private static Function<BlockPos, RenderSection> getRenderSection;
    private static AsynchronousChunkCollector chunkContext;

    public static void fromSectionManager(Long2ReferenceMap<RenderSection> sections, Level world) {
        SodiumAsyncSectionUtil.occlusionCuller = new OcclusionCuller(sections, world);
    }

    public static void asyncSearchRebuildSection() {
        if(shouldSearch) {
            shouldSearch = false;

            frame++;
            AsynchronousChunkCollector chunkCollector = new AsynchronousChunkCollector(frame);
            occlusionCuller.findVisible(chunkCollector, viewport, searchDistance, useOcclusionCulling, frame);
            chunkContext = chunkCollector;
        }

        /*
        Set<RenderSection> chunkUpdates = atomicBfsResult.get() == null ? new HashSet<>() : atomicBfsResult.get();

        if(atomicBfsResult.get() == null) {
            atomicBfsResult.set(chunkUpdates);
        }

        final OcclusionCuller.Visitor visitor = (section, visible) -> {
            if (section.getPendingUpdate() != null && section.getBuildCancellationToken() == null) {
                if ((!((IRenderSectionVisibility) section).isSubmittedRebuild()) && !((IRenderSectionVisibility) section).isSearched()) {//If it is in submission queue or seen dont enqueue
                    //Set that the section has been seen
                    ((IRenderSectionVisibility) section).setSearch(true);

                }
                if(!chunkUpdates.contains(section))
                    chunkUpdates.add(section);
            }
        };

        try {
            occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame ++);
        } catch (Throwable e) {
            System.err.println("Error doing traversal");
            e.printStackTrace();
        }
         */
    }

    public static void update(Viewport viewport, float searchDistance, boolean useOcclusionCulling) {
        SodiumAsyncSectionUtil.viewport = viewport;
        SodiumAsyncSectionUtil.searchDistance = searchDistance;
        SodiumAsyncSectionUtil.useOcclusionCulling = useOcclusionCulling;
        SodiumAsyncSectionUtil.shouldSearch = true;
    }

    public static ChunkContext handleRenderSearch(int frame, Viewport viewport, OcclusionCuller occlusionCuller, VisibleChunkCollector visitor, Function<BlockPos, RenderSection> getRenderSection) {
        SodiumAsyncSectionUtil.getRenderSection = getRenderSection;
        SodiumAsyncSectionUtil.sodiumLastChunkUpdateFrame = frame;

        if(chunkContext != null)
            return new ChunkContext(chunkContext.createRenderLists(frame), chunkContext.getRebuildLists());

        long time = System.nanoTime();
        //occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame ++);
        for (BlockPos pos : CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks()) {
            RenderSection section = getRenderSection.apply(pos);
            if (section != null)
                visitor.visit(section, viewport.isBoxVisible(pos.getX() << 4, pos.getY() << 4, pos.getZ() << 4, 16));
        }

        Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists = new EnumMap(ChunkUpdateType.class);
        ChunkUpdateType[] var2 = ChunkUpdateType.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ChunkUpdateType type = var2[var4];
            rebuildLists.put(type, new ArrayDeque());
        }
        var bfsResult = atomicBfsResult.getAndSet(null);
        if (bfsResult != null && !bfsResult.isEmpty()) {
            for (var section : bfsResult) {
                if (section.isDisposed())
                    continue;
                var type = section.getPendingUpdate();
                /*
                if (type != null && section.getBuildCancellationToken() == null) {
                    var queue = rebuildLists.get(type);
                    if (queue.size() < type.getMaximumQueueSize()) {
                        ((IRenderSectionVisibility) section).setSubmittedRebuild(true);
                        queue.add(section);
                    }
                }
                //Reset that the section has not been seen (whether its been submitted to the queue or not)
                ((IRenderSectionVisibility) section).setSearch(false);
                 */
            }
        }

        long end = System.nanoTime() - time;
        return new ChunkContext(visitor.createRenderLists(), rebuildLists);
    }

    public static Map<ChunkUpdateType, ArrayDeque<RenderSection>> handleBuildSearch() {
        if(true) return null;
        long time = System.nanoTime();
        Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists = new EnumMap(ChunkUpdateType.class);
        ChunkUpdateType[] var2 = ChunkUpdateType.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ChunkUpdateType type = var2[var4];
            rebuildLists.put(type, new ArrayDeque());
        }

        /*
        var bfsResult = atomicBfsResult.getAndSet(null);
        if (bfsResult != null) {
            for (var section : bfsResult) {
                if (section.isDisposed())
                    continue;
                var type = section.getPendingUpdate();
                if (type != null && section.getBuildCancellationToken() == null) {
                    var queue = rebuildLists.get(type);
                    if (queue.size() < type.getMaximumQueueSize()) {
                        ((IRenderSectionVisibility) section).setSubmittedRebuild(true);
                        queue.add(section);
                    }
                }
                //Reset that the section has not been seen (whether its been submitted to the queue or not)
                ((IRenderSectionVisibility) section).setSearch(false);
            }
        }
         */

        shouldSearch = true;
        long end = System.nanoTime() - time;
        return rebuildLists;
    }

    public static RenderSection getSectionFromPos(BlockPos pos) {
        if(getRenderSection != null) {
            return getRenderSection.apply(pos);
        }
        return null;
    }

    public static int getSodiumLastChunkUpdateFrame() {
        return sodiumLastChunkUpdateFrame;
    }

    public record ChunkContext(SortedRenderLists renderLists, Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists) {}

    public static class AsynchronousChunkCollector implements OcclusionCuller.Visitor {
        private final ObjectArrayList<ChunkRenderList> sortedRenderLists;
        private final EnumMap<ChunkUpdateType, ArrayDeque<RenderSection>> sortedRebuildLists;
        private final int frame;
        private final HashMap<RenderRegion, ChunkRenderList> renderListMap = new HashMap<>();
        private final Queue<RenderSection> renderSections = new ArrayDeque<>();

        public AsynchronousChunkCollector(int frame) {
            this.frame = frame;
            this.sortedRenderLists = new ObjectArrayList();
            this.sortedRebuildLists = new EnumMap(ChunkUpdateType.class);
            ChunkUpdateType[] var2 = ChunkUpdateType.values();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ChunkUpdateType type = var2[var4];
                this.sortedRebuildLists.put(type, new ArrayDeque());
            }
        }

        public void visit(RenderSection section, boolean visible) {
            RenderRegion region = section.getRegion();
            ChunkRenderList renderList;
            if(!renderListMap.containsKey(region)) {
                renderList = new ChunkRenderList(region);
                renderList.reset(frame);
                this.sortedRenderLists.add(renderList);
                renderListMap.put(region, renderList);
            } else {
                renderList = renderListMap.get(region);
            }

            if (visible && section.getFlags() != 0) {
                renderList.add(section);
                renderSections.add(section);
            }

            this.addToRebuildLists(section);
        }

        private void addToRebuildLists(RenderSection section) {
            ChunkUpdateType type = section.getPendingUpdate();
            if (type != null && section.getBuildCancellationToken() == null) {
                Queue<RenderSection> queue = (Queue)this.sortedRebuildLists.get(type);
                if (queue.size() < type.getMaximumQueueSize()) {
                    queue.add(section);
                }
            }
        }

        public SortedRenderLists createRenderLists(int frame) {
            for (RenderSection section : renderSections) {
                section.setLastVisibleFrame(frame);
            }

            for (ChunkRenderList list : sortedRenderLists) {
                ((IChunkRenderList)list).setLastVisibleFrame(frame);
            }

            try {
                Constructor<?> constructor = SortedRenderLists.class.getDeclaredConstructor(ObjectArrayList.class);
                constructor.setAccessible(true);
                SortedRenderLists sortedRenderLists1 = (SortedRenderLists) constructor.newInstance(this.sortedRenderLists);
                return sortedRenderLists1;
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException ignored) {

            }
            return new SortedRenderLists.Builder(frame).build();
        }

        public Map<ChunkUpdateType, ArrayDeque<RenderSection>> getRebuildLists() {
            return this.sortedRebuildLists;
        }
    }
}

package rogo.renderingculling.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import net.minecraft.core.BlockPos;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.IDualityChunkRenderList;
import rogo.renderingculling.api.VisibleChunkUploader;
import rogo.renderingculling.mixin.InvokerRenderSectionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class SodiumChunkUploader extends VisibleChunkUploader<VisibleChunkCollector> {
    public static int frame = 0;

    @Override
    public void update() {
        try {
            AsynchronousChunkCollector visitor = new AsynchronousChunkCollector(++frame);

            RenderSectionManager manager = ((InvokerRenderSectionManager.AccessorSodiumWorldRenderer)SodiumWorldRenderer.instance()).getRenderSectionManager();
            Set<BlockPos> queue = CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks();
            for(BlockPos pos : queue) {
                RenderSection section = ((InvokerRenderSectionManager)manager).invokeGetRenderSection(pos.getX(), pos.getY(), pos.getZ());
                if(section != null) {
                    visitor.visit(section, true);
                }
            }

            CullingHandler.CHUNK_CULLING_MAP.setUploaderResult(visitor);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static AsynchronousChunkCollector hot(int frame, Function<BlockPos, RenderSection> function) {

        AsynchronousChunkCollector visitor = new AsynchronousChunkCollector(frame);

        Set<BlockPos> queue = CullingHandler.CHUNK_CULLING_MAP.getVisibleChunks();
        for(BlockPos pos : queue) {
            RenderSection section = function.apply(pos);
            if(section != null) {
                //visitor.visit(section, true);
            }
        }


        return null;
    }

    public static class AsynchronousChunkCollector implements OcclusionCuller.Visitor {
        private final ObjectArrayList<ChunkRenderList> sortedRenderLists;
        private final EnumMap<ChunkUpdateType, ArrayDeque<RenderSection>> sortedRebuildLists;
        private final int frame;
        private final HashMap<RenderRegion, ChunkRenderList> renderListMap = new HashMap<>();

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
                renderListMap.put(region, renderList);
            } else {
                renderList = renderListMap.get(region);
            }

            if(!this.sortedRenderLists.contains(renderList))
                this.sortedRenderLists.add(renderList);

            if (visible && section.getFlags() != 0) {
                section.setLastVisibleFrame(frame);
                section.setIncomingDirections(0);
                renderList.add(section);
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
            for(ChunkRenderList renderList : this.sortedRenderLists) {
                //((IDualityChunkRenderList)renderList).observer(frame);
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
package rogo.renderingculling.util;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkTracker;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphInfo;
import me.jellysquid.mods.sodium.client.render.chunk.graph.ChunkGraphIterationQueue;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SodiumSectionAsyncUtil {
    private static int renderDistance;
    private static double fogRenderCutoff;
    private static boolean useFogCulling;
    private static boolean useOcclusionCulling;
    private static Camera camera;
    private static Frustum frustum;
    private static Camera shadowCamera;
    private static Frustum shadowFrustum;
    private static AsynchronousChunkCollector collector;
    private static AsynchronousChunkCollector shadowCollector;
    public static boolean renderingEntities;
    private static final Semaphore shouldUpdate = new Semaphore(0);
    private static Long2ReferenceMap<RenderSection> sections;
    private static ChunkTracker tracker;
    public static boolean needSyncRebuild;
    public static int frame;

    public static void fromSectionManager(Long2ReferenceMap<RenderSection> sections, ChunkTracker tracker, int renderDistance) {
        SodiumSectionAsyncUtil.sections = sections;
        SodiumSectionAsyncUtil.tracker = tracker;
        SodiumSectionAsyncUtil.renderDistance = renderDistance;
    }

    public static void asyncSearchRebuildSection() {
        shouldUpdate.acquireUninterruptibly();
        if (camera != null && frustum != null) {
            if (CullingStateManager.enabledShader() && shadowCamera != null && shadowFrustum != null) {
                frame++;
                CullingStateManager.useOcclusionCulling = false;
                AsynchronousChunkCollector shadowCollector = new AsynchronousChunkCollector(frame, shadowCamera, shadowFrustum);
                shadowCollector.findVisible();
                SodiumSectionAsyncUtil.shadowCollector = shadowCollector;
                CullingStateManager.useOcclusionCulling = true;
            }

            frame++;
            AsynchronousChunkCollector collector = new AsynchronousChunkCollector(frame, camera, frustum);
            collector.findVisible();
            SodiumSectionAsyncUtil.collector = collector;

            if (CullingStateManager.CHUNK_CULLING_MAP != null)
                CullingStateManager.CHUNK_CULLING_MAP.queueUpdateCount++;
            Map<ChunkUpdateType, PriorityQueue<RenderSection>> rebuildList = SodiumSectionAsyncUtil.collector.getRebuildLists();
            for (PriorityQueue<RenderSection> arrayDeque : rebuildList.values()) {
                if (!arrayDeque.isEmpty()) {
                    needSyncRebuild = true;
                    break;
                }
            }
        }
    }

    public static void update(Camera camera, Frustum frustum, double fogRenderCutoff, boolean useFogCulling, boolean useOcclusionCulling) {
        if (CullingStateManager.renderingShader()) {
            SodiumSectionAsyncUtil.shadowCamera = camera;
            SodiumSectionAsyncUtil.shadowFrustum = frustum;
        } else {
            SodiumSectionAsyncUtil.camera = camera;
            SodiumSectionAsyncUtil.frustum = frustum;
        }
        SodiumSectionAsyncUtil.fogRenderCutoff = fogRenderCutoff;
        SodiumSectionAsyncUtil.useFogCulling = useFogCulling;
        SodiumSectionAsyncUtil.useOcclusionCulling = useOcclusionCulling;
    }

    public static AsynchronousChunkCollector getChunkCollector() {
        return SodiumSectionAsyncUtil.collector;
    }

    public static AsynchronousChunkCollector getShadowCollector() {
        return SodiumSectionAsyncUtil.shadowCollector;
    }

    public static void shouldUpdate() {
        if (shouldUpdate.availablePermits() < 1) {
            shouldUpdate.release();
        }
    }

    protected static RenderSection getRenderSection(int x, int y, int z) {
        return sections.get(SectionPos.asLong(x, y, z));
    }

    public static boolean shouldCancelBuild(RenderSection section) {
        return section == null || section.getRegion().getArenas() == null;
    }

    public static class AsynchronousChunkCollector {
        private final Map<ChunkUpdateType, PriorityQueue<RenderSection>> rebuildLists = new EnumMap<>(ChunkUpdateType.class);
        private final ChunkRenderList chunkRenderList = new ChunkRenderList();
        private final ChunkGraphIterationQueue iterationQueue = new ChunkGraphIterationQueue();
        private final ObjectList<RenderSection> tickableChunks = new ObjectArrayList<>();
        private final ObjectList<BlockEntity> visibleBlockEntities = new ObjectArrayList<>();
        private final Camera camera;
        private final Frustum frustum;
        private final int currentFrame;
        private int centerChunkX;
        private int centerChunkZ;
        private final Level level = Minecraft.getInstance().level;

        public AsynchronousChunkCollector(int currentFrame, Camera camera, Frustum frustum) {
            ChunkUpdateType[] var6 = ChunkUpdateType.values();
            for (ChunkUpdateType type : var6) {
                this.rebuildLists.put(type, new ObjectArrayFIFOQueue<>());
            }
            this.camera = camera;
            this.frustum = frustum;
            this.currentFrame = currentFrame;
        }

        public void findVisible() {
            if (level == null) {
                return;
            }
            ChunkGraphIterationQueue queue = this.iterationQueue;
            BlockPos pos = getOriginPos();
            this.centerChunkX = pos.getX() >> 4;
            this.centerChunkZ = pos.getZ() >> 4;
            RenderSection rootRender = SodiumSectionAsyncUtil.getRenderSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
            rootRender.getGraphInfo().resetCullingState();
            rootRender.getGraphInfo().setLastVisibleFrame(this.currentFrame);
            addVisible(rootRender, null);
            for (int i = 0; i < queue.size(); ++i) {
                RenderSection section = queue.getRender(i);
                short cullData = section.getGraphInfo().computeQueuePop();
                this.schedulePendingUpdates(section);
                for (Direction dir : DirectionUtil.ALL_DIRECTIONS) {
                    if (!useOcclusionCulling || (cullData & 1 << dir.ordinal()) != 0) {
                        RenderSection adj = section.getAdjacent(dir);
                        if (adj != null && this.isWithinRenderDistance(adj) && this.isVisible(adj)) {
                            this.bfsEnqueue(adj, DirectionUtil.getOpposite(dir), cullData);
                        }
                    }
                }
            }
        }

        private void bfsEnqueue(RenderSection render, Direction flow, short parentalData) {
            ChunkGraphInfo info = render.getGraphInfo();
            if (info.getLastVisibleFrame() == this.currentFrame) {
                info.updateCullingState(flow, parentalData);
            } else {
                info.setLastVisibleFrame(this.currentFrame);
                info.setCullingState(parentalData);
                info.updateCullingState(flow, parentalData);
                this.addVisible(render, flow);
            }
        }

        private void addVisible(RenderSection render, Direction flow) {
            this.iterationQueue.add(render, flow);
            if (!useFogCulling || !(render.getSquaredDistanceXZ(this.camera.getPosition().x, this.camera.getPosition().z) >= fogRenderCutoff)) {
                if (!render.isEmpty()) {
                    this.chunkRenderList.add(render);
                    if (render.isTickable()) {
                        this.tickableChunks.add(render);
                    }
                    if (!render.getData().getBlockEntities().isEmpty()) {
                        this.visibleBlockEntities.addAll(render.getData().getBlockEntities());
                    }
                }

            }
        }

        public boolean isVisible(RenderSection section) {
            return !section.getGraphInfo().isCulledByFrustum(this.frustum) && CullingStateManager.shouldRenderChunk((IRenderSectionVisibility) section, true);
        }

        private void schedulePendingUpdates(RenderSection section) {
            if (section.getPendingUpdate() != null && SodiumSectionAsyncUtil.tracker.hasMergedFlags(section.getChunkX(), section.getChunkZ(), 3)) {
                PriorityQueue<RenderSection> queue = this.rebuildLists.get(section.getPendingUpdate());
                if (queue.size() < 32) {
                    queue.enqueue(section);
                }
            }
        }

        public Map<ChunkUpdateType, PriorityQueue<RenderSection>> getRebuildLists() {
            return rebuildLists;
        }

        public ChunkRenderList getChunkRenderList() {
            return chunkRenderList;
        }

        public ObjectList<BlockEntity> getVisibleBlockEntities() {
            return visibleBlockEntities;
        }

        public ObjectList<RenderSection> getTickableChunks() {
            return tickableChunks;
        }

        @NotNull
        private static BlockPos getOriginPos() {
            BlockPos origin = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
            if (origin.getY() < Minecraft.getInstance().level.getMinBuildHeight()) {
                origin = new BlockPos(origin.getX(), Minecraft.getInstance().level.getMinBuildHeight(), origin.getZ());
            } else if (origin.getY() > Minecraft.getInstance().level.getMaxBuildHeight()) {
                origin = new BlockPos(origin.getX(), Minecraft.getInstance().level.getMaxBuildHeight(), origin.getZ());
            }
            return origin;
        }

        private boolean isWithinRenderDistance(RenderSection adj) {
            int x = Math.abs(adj.getChunkX() - this.centerChunkX);
            int z = Math.abs(adj.getChunkZ() - this.centerChunkZ);
            return x <= SodiumSectionAsyncUtil.renderDistance && z <= SodiumSectionAsyncUtil.renderDistance;
        }
    }
}
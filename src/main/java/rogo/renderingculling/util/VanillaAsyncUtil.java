package rogo.renderingculling.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.CullingHandler;
import rogo.renderingculling.api.impl.IEntitiesForRender;
import rogo.renderingculling.api.impl.IRenderChunkInfo;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.mixin.MixinLevelRender;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

public class VanillaAsyncUtil {
    private static int chunkLength = 0;
    private static LevelRenderer.RenderChunkStorage storage;
    private static ObjectArrayList<ChunkRenderDispatcher.RenderChunk> rebuildChunksInFrustum;
    private static ObjectArrayList<ChunkRenderDispatcher.RenderChunk> rebuildChunksTemp;
    private static LevelRenderer levelRenderer;

    public static void asyncSearchRebuildSection() {
        if(levelRenderer == null) return;
        LevelRenderer.RenderChunkStorage renderChunkStorage = new LevelRenderer.RenderChunkStorage(chunkLength);
        ObjectArrayList<ChunkRenderDispatcher.RenderChunk> rebuildList = new ObjectArrayList<>();
        Queue<LevelRenderer.RenderChunkInfo> queue = new ArrayDeque<>();
        HashSet<BlockPos> posHashSet = new HashSet<>();
        BlockPos origin = getOriginPos();
        LevelRenderer.RenderChunkInfo originChunk = new LevelRenderer.RenderChunkInfo(((IEntitiesForRender) levelRenderer).invokeGetRenderChunkAt(origin), null, 0);
        queue.add(originChunk);
        ChunkRenderDispatcher.RenderChunk originRenderChunk = ((IRenderChunkInfo) originChunk).getRenderChunk();
        rebuildList.add(originRenderChunk);
        while (!queue.isEmpty()) {
            LevelRenderer.RenderChunkInfo last = queue.poll();
            ChunkRenderDispatcher.RenderChunk lastRenderChunk = ((IRenderChunkInfo) last).getRenderChunk();
            if (originChunk != last && (!CullingHandler.FRUSTUM.isVisible(lastRenderChunk.getBoundingBox()) || !CullingHandler.shouldRenderChunk((IRenderSectionVisibility) lastRenderChunk, false)))
                continue;
            renderChunkStorage.renderChunks.add(last);
            if (lastRenderChunk.isDirty() && Minecraft.getInstance().level.getLightEngine().lightOnInSection(SectionPos.of(lastRenderChunk.getOrigin()))) {
                rebuildList.add(lastRenderChunk);
            }
            for (Direction direction : DIRECTIONS) {
                ChunkRenderDispatcher.RenderChunk sideRenderChunk = ((IEntitiesForRender) levelRenderer).invokeGetRelativeFrom(origin, lastRenderChunk, direction);
                if (sideRenderChunk != null && (!last.hasDirection(direction.getOpposite())) && !posHashSet.contains(sideRenderChunk.getOrigin())) {
                    posHashSet.add(sideRenderChunk.getOrigin());
                    LevelRenderer.RenderChunkInfo newRenderChunk = new LevelRenderer.RenderChunkInfo(sideRenderChunk, direction, ((MixinLevelRender.AccessorRenderChunkInfo) last).getStep() + 1);
                    newRenderChunk.setDirections(((MixinLevelRender.AccessorRenderChunkInfo) last).getDirections(), direction);
                    queue.add(newRenderChunk);
                    //renderChunkStorage.renderInfoMap.put(sideRenderChunk, newRenderChunk);
                }
            }
        }
        storage = renderChunkStorage;
        rebuildChunksInFrustum = rebuildList;
    }

    public static void update(LevelRenderer renderer, int length) {
        levelRenderer = renderer;
        chunkLength = length;
    }

    public static LevelRenderer.RenderChunkStorage getChunkStorage() {
        return storage;
    }

    public static ObjectArrayList<ChunkRenderDispatcher.RenderChunk> getRebuildChunk() {
        return rebuildChunksInFrustum;
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
}
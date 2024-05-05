package rogo.renderingculling.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import rogo.renderingculling.api.Config;

import java.util.concurrent.Semaphore;

public class VanillaAsyncUtil {
    private static final Semaphore shouldUpdate = new Semaphore(0);
    public static boolean injectedAsyncMixin;

    public static void asyncSearchRebuildSection() {

    }

    public static void update(LevelRenderer renderer, int length) {
    }

    public static SectionOcclusionGraph getChunkStorage() {
        return null;
    }

    public static boolean shouldReplaceStorage() {
        return Config.getAsyncChunkRebuild() && getChunkStorage() != null;
    }

    public static void shouldUpdate() {
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
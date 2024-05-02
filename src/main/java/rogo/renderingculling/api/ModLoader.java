package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import rogo.renderingculling.api.impl.IAABBObject;
import rogo.renderingculling.event.WorldUnloadEvent;
import rogo.renderingculling.gui.ConfigScreen;
import rogo.renderingculling.mixin.AccessorFrustum;
import rogo.renderingculling.util.NvidiumUtil;
import rogo.renderingculling.util.OcclusionCullerThread;

import java.io.IOException;
import java.util.function.Supplier;

import static java.lang.Thread.MAX_PRIORITY;
import static rogo.renderingculling.api.CullingStateManager.*;

public class ModLoader implements ModInitializer {

    public static boolean SHADER_ENABLED = false;

    @Override
    public void onInitialize() {
        callWhenOn(EnvType.CLIENT, () -> () -> {
            registerEvents();
            this.registerShader();
            init();
        });
    }

    public static void callWhenOn(EnvType envType, Supplier<Runnable> supplier) {
        if (envType == FabricLoader.getInstance().getEnvironmentType()) {
            supplier.get().run();
        }
    }

    private void registerShader() {
        RenderSystem.recordRenderCall(this::initShader);
    }

    private void initShader() {
        LOGGER.debug("try init shader chunk_culling");
        try {
            SHADER_ENABLED = true;
            CHUNK_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), fromID("chunk_culling"), DefaultVertexFormat.POSITION);
            INSTANCED_ENTITY_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), fromID("instanced_entity_culling"), DefaultVertexFormat.POSITION);
            COPY_DEPTH_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), fromID("copy_depth"), DefaultVertexFormat.POSITION);
            REMOVE_COLOR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), fromID("remove_color"), DefaultVertexFormat.POSITION_COLOR_TEX);
            SHADER_ENABLED = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromID(String s) {
        return MOD_ID + ":" + s;
    }

    private void registerEvents() {
        WorldUnloadEvent.WORLD_UNLOAD.register(this::onWorldUnload);
        ClientTickEvents.START_CLIENT_TICK.register(this::onStartClientTick);
    }

    private void onWorldUnload(Level world) {
        if (world == Minecraft.getInstance().level) {
            cleanup();
        }
    }

    private void onStartClientTick(Minecraft client) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            Config.loadConfig();
            clientTickCount++;
            if (Minecraft.getInstance().player.tickCount > 60 && clientTickCount > 60 && CHUNK_CULLING_MAP != null && !CHUNK_CULLING_MAP.isDone()) {
                CHUNK_CULLING_MAP.setDone();
                LEVEL_SECTION_RANGE = Minecraft.getInstance().level.getMaxSection() - Minecraft.getInstance().level.getMinSection();
                LEVEL_MIN_SECTION_ABS = Math.abs(Minecraft.getInstance().level.getMinSection());
                LEVEL_MIN_POS = Minecraft.getInstance().level.getMinBuildHeight();
                LEVEL_POS_RANGE = Minecraft.getInstance().level.getMaxBuildHeight() - Minecraft.getInstance().level.getMinBuildHeight();

                OcclusionCullerThread occlusionCullerThread = new OcclusionCullerThread();
                occlusionCullerThread.setName("Chunk Depth Occlusion Cull thread");
                occlusionCullerThread.setPriority(MAX_PRIORITY);
                occlusionCullerThread.start();
            }
        } else {
            cleanup();
        }
    }

    public static final KeyMapping CONFIG_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(MOD_ID + ".key.config",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    "key.category." + MOD_ID));

    public static final KeyMapping DEBUG_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(MOD_ID + ".key.debug",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    "key.category." + MOD_ID));

    public static void onKeyPress() {
        if (CONFIG_KEY.isDown()) {
            Minecraft.getInstance().setScreen(new ConfigScreen(Component.translatable(MOD_ID + ".config")));
        }
        if (DEBUG_KEY.isDown()) {
            DEBUG++;
            if (DEBUG >= 3)
                DEBUG = 0;
        }
    }

    public static Vector4f[] getFrustumPlanes(FrustumIntersection frustum) {
        return ((AccessorFrustum.AccessorFrustumIntersection) frustum).planes();
    }

    public static boolean hasMod(String s) {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(modInfo -> modInfo.getMetadata().getId().equals(s));
    }

    public static boolean hasSodium() {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(modInfo -> modInfo.getMetadata().getId().equals("sodium") || modInfo.getMetadata().getId().equals("embeddium") || modInfo.getMetadata().getId().equals("rubidium"));
    }

    public static boolean hasIris() {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(modInfo -> modInfo.getMetadata().getId().equals("iris") || modInfo.getMetadata().getId().equals("oculus"));
    }

    public static boolean hasNvidium() {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(modInfo -> modInfo.getMetadata().getId().equals("nvidium")) && NvidiumUtil.nvidiumBfs();
    }

    public static AABB getObjectAABB(Object o) {
        if (o instanceof BlockEntity) {
            return new AABB(((BlockEntity) o).getBlockPos());
        } else if (o instanceof Entity) {
            return ((Entity) o).getBoundingBox();
        } else if (o instanceof IAABBObject) {
            return ((IAABBObject) o).getAABB();
        }

        return null;
    }

    public static void pauseAsync() {
        if (ModLoader.hasMod("embeddium")) {
            fullChunkUpdateCooldown = 60;
        }
    }
}

package rogo.renderingculling.api;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;
import rogo.renderingculling.api.impl.IAABBObject;
import rogo.renderingculling.gui.ConfigScreen;
import rogo.renderingculling.util.NvidiumUtil;
import rogo.renderingculling.util.OcclusionCullerThread;

import java.io.IOException;

import static java.lang.Thread.MAX_PRIORITY;
import static rogo.renderingculling.api.CullingStateManager.*;

@Mod("brute_force_rendering_culling")
public class ModLoader {

    public ModLoader() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            registerShader();
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new CullingRenderEvent());
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBinding);

            CullingStateManager.init();
        });
    }

    public static final KeyMapping CONFIG_KEY = new KeyMapping(MOD_ID + ".key.config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.category." + MOD_ID);

    public static final KeyMapping DEBUG_KEY = new KeyMapping(MOD_ID + ".key.debug",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.category." + MOD_ID);

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
        event.register(DEBUG_KEY);
        //event.register(TEST_CULL_KEY);
    }

    private void registerShader() {
        RenderSystem.recordRenderCall(this::initShader);
    }

    public static ShaderInstance CULL_TEST_SHADER;
    public static RenderTarget CULL_TEST_TARGET;

    static {
        CULL_TEST_TARGET = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false, Minecraft.ON_OSX);
        CULL_TEST_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    }

    private void initShader() {
        LOGGER.debug("try init shader chunk_culling");
        try {
            CHUNK_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "chunk_culling"), DefaultVertexFormat.POSITION);
            INSTANCED_ENTITY_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "instanced_entity_culling"), DefaultVertexFormat.POSITION);
            COPY_DEPTH_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "copy_depth"), DefaultVertexFormat.POSITION);
            REMOVE_COLOR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "remove_color"), DefaultVertexFormat.POSITION_COLOR_TEX);
            CULL_TEST_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "culling_test"), DefaultVertexFormat.POSITION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player != null) {
            if (CONFIG_KEY.isDown()) {
                Minecraft.getInstance().setScreen(new ConfigScreen(ComponentUtil.translatable(MOD_ID + ".config")));
            }
            if (DEBUG_KEY.isDown()) {
                DEBUG++;
                if (DEBUG >= 3)
                    DEBUG = 0;
            }
        }
    }

    public static void onKeyPress() {
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
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
                Config.setLoaded();
            } else {
                cleanup();
            }
        }
    }

    public static boolean hasMod(String s) {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals(s));
    }

    public static boolean hasSodium() {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("sodium") || modInfo.getModId().equals("embeddium") || modInfo.getModId().equals("rubidium"));
    }

    public static boolean hasIris() {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("iris") || modInfo.getModId().equals("oculus"));
    }

    public static boolean hasNvidium() {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("nvidium")) && NvidiumUtil.nvidiumBfs();
    }

    public static AABB getObjectAABB(Object o) {
        if (o instanceof BlockEntity) {
            return ((BlockEntity) o).getRenderBoundingBox();
        } else if (o instanceof Entity) {
            return ((Entity) o).getBoundingBox();
        } else if (o instanceof IAABBObject) {
            return ((IAABBObject) o).getAABB();
        }

        return null;
    }

    public static void pauseAsync() {
    }
}

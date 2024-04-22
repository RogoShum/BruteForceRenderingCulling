package rogo.renderingculling.api;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rogo.renderingculling.gui.ConfigScreen;
import rogo.renderingculling.util.OcclusionCullerThread;

import java.io.IOException;

import static java.lang.Thread.MAX_PRIORITY;
import static rogo.renderingculling.api.CullingHandler.*;

@Mod("brute_force_rendering_culling")
public class ModLoader {

    public ModLoader() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            registerShader();
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new CullingRenderEvent());
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBinding);

            CullingHandler.init();
        });
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_KEY);
        event.register(DEBUG_KEY);
    }

    private void registerShader() {
        RenderSystem.recordRenderCall(this::initShader);
    }

    private void initShader() {
        LOGGER.debug("try init shader chunk_culling");
        try {
            CHUNK_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "chunk_culling"), DefaultVertexFormat.POSITION);
            INSTANCED_ENTITY_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "instanced_entity_culling"), DefaultVertexFormat.POSITION);
            COPY_DEPTH_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "copy_depth"), DefaultVertexFormat.POSITION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player != null) {
            if (CONFIG_KEY.isDown()) {
                Minecraft.getInstance().setScreen(new ConfigScreen(Component.translatable(MOD_ID + ".config")));
            }
            if (DEBUG_KEY.isDown()) {
                DEBUG++;
                if (DEBUG >= 3)
                    DEBUG = 0;
            }
        }
    }

    @SubscribeEvent
    public void onTooltip(RenderTooltipEvent.Color event) {
        if (reColorToolTip) {
            int BG = ((50 & 0xFF) << 24) |
                    ((0) << 16) |
                    ((0) << 8) |
                    ((0));

            int B = ((100 & 0xFF) << 24) |
                    ((0xFF) << 16) |
                    ((0xFF) << 8) |
                    ((0xFF));
            event.setBackgroundStart(BG);
            event.setBackgroundEnd(BG);
            event.setBorderStart(B);
            event.setBorderEnd(B);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
                clientTickCount++;
                if (clientTickCount > 200 && CHUNK_CULLING_MAP != null && !CHUNK_CULLING_MAP.isDone()) {
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
}

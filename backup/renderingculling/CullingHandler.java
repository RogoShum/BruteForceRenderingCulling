package rogo.renderingculling;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
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
import org.slf4j.Logger;
import rogo.renderingculling.gui.ConfigScreen;
import rogo.renderingculling.mixin.AccessorLevelRender;

import java.io.IOException;
import java.util.*;

@Mod("brute_force_rendering_culling")
public class CullingHandler {
    public static CullingHandler INSTANCE;
    public static final String MOD_ID = "brute_force_rendering_culling";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static DepthMap DEPTH_MAP = null;
    private static DepthMap DEPTH_MAP_STATIC = null;
    protected static PoseStack VIEW_MATRIX = new PoseStack();
    public static Matrix4f PROJECTION_MATRIX = new Matrix4f();

    static {
        PROJECTION_MATRIX.setIdentity();
    }

    public static RenderTarget DEPTH_TARGET;
    public static RenderTarget CULLING_TARGET;
    private static ShaderInstance CHUNK_CULLING_SHADER;
    public static Frustum FRUSTUM;
    public static boolean CULLING = true;
    private boolean DEBUG = false;
    public static int DEPTH_TEXTURE;
    public static ShaderLoader SHADER_LOADER = null;
    public static Class<?> OptiFine = null;

    public HashSet<Entity> culledEntity = new HashSet<>();
    public HashSet<BlockPos> culledBlock = new HashSet<>();
    public HashSet<BlockPos> culledChunk = new HashSet<>();
    public LifeTimer<Entity> visibleEntity = new LifeTimer<>();
    public LifeTimer<BlockPos> visibleBlock = new LifeTimer<>();
    public LifeTimer<BlockPos> visibleChunk = new LifeTimer<>();
    private boolean nextTick = true;
    private int tick = 0;
    private int clientTickCount = 0;
    private int fontCount = 0;
    private int entityCulling = 0;
    private int entityCount = 0;
    private int blockCulling = 0;
    private int blockCount = 0;
    private long entityCullingTime = 0;
    private long blockCullingTime = 0;
    private long chunkCullingTime = 0;
    private long preEntityCullingTime = 0;
    private long preBlockCullingTime = 0;
    private long preChunkCullingTime = 0;
    private int chunkCulling = 0;
    private int chunkCount = 0;
    private long initTime = 0;
    private long preInitTime = 0;
    public boolean applyFrustum = false;
    public boolean checkCulling = false;
    private boolean nextLoop = false;
    public static Camera camera;
    private Vector3f lastCameraVec;

    static {
        RenderSystem.recordRenderCall(() -> {
            DEPTH_TARGET = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), true, Minecraft.ON_OSX);
            DEPTH_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            CULLING_TARGET = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), true, Minecraft.ON_OSX);
            CULLING_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        });
    }

    public CullingHandler() {
        INSTANCE = this;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(INSTANCE::doClientStuff);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(INSTANCE::registerShader);
            try {
                OptiFine = Class.forName("net.optifine.shaders.Shaders");
            } catch (ClassNotFoundException e) {
                LOGGER.debug("OptiFine Not Found");
            }

            if (OptiFine != null) {
                try {
                    SHADER_LOADER = Class.forName("rogo.renderingculling.OptiFineLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
                }
            }

            if (FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("oculus"))) {
                try {
                    SHADER_LOADER = Class.forName("rogo.renderingculling.IrisLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(CONFIG_KEY);
            ClientRegistry.registerKeyBinding(DEBUG_KEY);
        });
    }

    private void registerShader(final ModelRegistryEvent event) {
        RenderSystem.recordRenderCall(this::initShader);
    }

    private void initShader() {
        LOGGER.debug("try init shader chunk_culling");
        try {
            CHUNK_CULLING_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "chunk_culling"), DefaultVertexFormat.POSITION_COLOR_NORMAL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void drawString(String text, int width, int height) {
        Font font = Minecraft.getInstance().font;
        font.drawShadow(new PoseStack(), text, width - (font.width(text) / 2f), height - font.lineHeight * fontCount, 16777215);
        fontCount++;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (Minecraft.getInstance().player != null) {
                clientTickCount++;
            } else {
                this.tick = 0;
                clientTickCount = 0;
                visibleChunk.clear();
                culledEntity.clear();
                culledBlock.clear();
                culledChunk.clear();
            }
        }
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.PostLayer event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (DEBUG && event.getOverlay() == ForgeIngameGui.HELMET_ELEMENT) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = minecraft.getWindow().getGuiScaledWidth() / 2;
            int height = 12;
            int widthScale = 80;

            int heightScale = -minecraft.font.lineHeight * fontCount;
            fontCount = 0;

            if (CullingHandler.CULLING) {
                String initTime = new TranslatableComponent("brute_force_rendering_culling.init").getString() + ": " + (this.initTime/1000) + " μs";
                drawString(initTime, width, height - heightScale);

                if (Config.CULL_CHUNK.get()) {
                    String chunkCullingTime = new TranslatableComponent("brute_force_rendering_culling.chunk_culling_time").getString() + ": " + (this.chunkCullingTime/1000) + " μs";
                    drawString(chunkCullingTime, width, height - heightScale);

                    String chunkCulling = new TranslatableComponent("brute_force_rendering_culling.chunk_culling").getString() + ": " + this.chunkCulling + " / " + this.chunkCount;
                    drawString(chunkCulling, width, height - heightScale);
                }

                String blockCullingTime = new TranslatableComponent("brute_force_rendering_culling.block_culling_time").getString() + ": " + (this.blockCullingTime/1000) + " μs";
                drawString(blockCullingTime, width, height - heightScale);

                String blockCulling = new TranslatableComponent("brute_force_rendering_culling.block_culling").getString() + ": " + this.blockCulling + " / " + this.blockCount;
                drawString(blockCulling, width, height - heightScale);

                String entityCullingTime = new TranslatableComponent("brute_force_rendering_culling.entity_culling_time").getString() + ": " + (this.entityCullingTime/1000) + " μs";
                drawString(entityCullingTime, width, height - heightScale);

                String entityCulling = new TranslatableComponent("brute_force_rendering_culling.entity_culling").getString() + ": " + this.entityCulling + " / " + this.entityCount;
                drawString(entityCulling, width, height - heightScale);
            }

            String Sampler = new TranslatableComponent("brute_force_rendering_culling.sampler").getString() + ": " + String.valueOf((Float.parseFloat(String.format("%.0f", Config.SAMPLING.get() * 100.0D))) + "%");
            drawString(Sampler, width, height - heightScale);

            String dp = new TranslatableComponent("brute_force_rendering_culling.depth_update").getString() + ": "
                    + (Config.UPDATE_DEPTH.get() ? new TranslatableComponent("brute_force_rendering_culling.enable").getString() : new TranslatableComponent("brute_force_rendering_culling.disable").getString());
            drawString(dp, width, height - heightScale);

            String cull = new TranslatableComponent("brute_force_rendering_culling.culling").getString() + ": "
                    + (CullingHandler.CULLING ? new TranslatableComponent("brute_force_rendering_culling.enable").getString() : new TranslatableComponent("brute_force_rendering_culling.disable").getString());
            drawString(cull, width, height - heightScale);

            int index = Minecraft.getInstance().fpsString.indexOf("fps");
            if (index != -1) {
                String extractedString = Minecraft.getInstance().fpsString.substring(0, index + 3);
                String fps = "FPS: " + extractedString;
                drawString(fps, width, height - heightScale);
            }

            height -= heightScale - minecraft.font.lineHeight;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_COLOR);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width + widthScale, height, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width + widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.vertex(width - widthScale, height + heightScale, 100.0D).color(0.3F, 0.3F, 0.3F, 0.2f).endVertex();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(width - widthScale - 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width + widthScale + 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.vertex(width - widthScale - 2, height + heightScale - 2, 90.0D).color(1.0F, 1.0F, 1.0F, 0.1f).endVertex();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();

            RenderSystem.setShader(()->CHUNK_CULLING_SHADER);

            if(LIGHT_TARGET.width != width/3 || LIGHT_TARGET.height != height/3) {
                LIGHT_TARGET.resize(width/3, height/3, Minecraft.ON_OSX);
            }
            LIGHT_TARGET.clear(Minecraft.ON_OSX);
            LIGHT_TARGET.bindWrite(false);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            RenderSystem.depthMask(false);

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(0.0D, (double)height, -50.0D).uv(0, 1).color(1, 1, 1, 1f).endVertex();
            bufferbuilder.vertex((double)width, (double)height, -50.0D).uv(1, 1).color(1, 1, 1, 1).endVertex();
            bufferbuilder.vertex((double)width, 0.0D, -50.0D).uv(1, 0).color(1, 1, 1, 1f).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -50.0D).uv(0, 0).color(1, 1, 1, 1).endVertex();
            tessellator.end();

            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            LIGHT_TARGET.blitToScreen(width, height, false);
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().player != null) {
            if (CONFIG_KEY.isDown()) {
                Minecraft.getInstance().setScreen(new ConfigScreen(new TranslatableComponent(MOD_ID + ".config")));
            }
            if (DEBUG_KEY.isDown()) {
                DEBUG = !DEBUG;
            }
        }
    }

    public boolean shouldSkipBlock(String type, AABB aabb, BlockPos pos) {
        if (DEPTH_MAP == null) return false;
        if (FRUSTUM == null || !FRUSTUM.isVisible(aabb)) return true;
        blockCount++;
        if (visibleBlock.contains(pos)) {
            return false;
        }
        if (!nextTick) {
            if (culledBlock.contains(pos)) {
                blockCulling++;
                return true;
            } else
                return false;
        }
        if (Config.BLOCK_ENTITY_SKIP.get().contains(type))
            return false;

        ScreenAABB entityVertices = getAABBPoints(aabb);
        long time = System.nanoTime();
        boolean skip = !DEPTH_MAP.isAABBVisible(entityVertices, VIEW_MATRIX.last().pose(),
                (float) (Vec3.atCenterOf(pos).distanceTo(camera.getPosition()) * 0.03125f));
        if (checkCulling)
            skip = !skip;
        preBlockCullingTime += System.nanoTime() - time;
        if (skip) {
            culledBlock.add(new BlockPos(pos));
            blockCulling++;
        } else
            visibleBlock.updateUsageTick(pos, clientTickCount);

        return skip;
    }

    public boolean shouldRenderChunk(AABB aabb, BlockPos pos) {
        if (!CULLING || !Config.CULL_CHUNK.get() || DEPTH_MAP_STATIC == null) return true;
        pos = (pos == null) ? new BlockPos(aabb.getCenter()) : pos;
        if (!nextTick) {
            chunkCount++;
            if (visibleChunk.contains(pos)) {
                return true;
            }
            if (culledChunk.contains(pos)) {
                chunkCulling++;
                return false;
            } else
                return true;
        }
        chunkCount++;
        ScreenAABB entityVertices = getAABBPoints(aabb);
        boolean render;
        long time = System.nanoTime();
        if (visibleChunk.contains(pos)) {
            render = true;
        } else
            render = DEPTH_MAP_STATIC.isChunkVisible(entityVertices, VIEW_MATRIX.last().pose(), 32);
        preChunkCullingTime += System.nanoTime() - time;
        if (checkCulling)
            render = !render;
        if (!render) {
            chunkCulling++;
            culledChunk.add(pos);
        } else
            visibleChunk.updateUsageTick(pos, clientTickCount);

        return render;
    }

    public boolean shouldRenderChunk(AABB aabb) {
        return shouldRenderChunk(aabb, null);
    }

    public boolean shouldSkipEntity(Entity entity) {
        if (entity instanceof Player || entity.isCurrentlyGlowing()) return false;
        if (entity.distanceToSqr(camera.getPosition()) < 4) return false;
        if (Config.ENTITY_SKIP.get().contains(entity.getType().getRegistryName().toString()))
            return false;
        if (DEPTH_MAP == null) return false;
        entityCount++;
        if (visibleEntity.contains(entity)) {
            return false;
        }
        if (!nextTick) {
            if (culledEntity.contains(entity)) {
                entityCulling++;
                return true;
            } else
                return false;
        }

        ScreenAABB entityVertices = getAABBPoints(entity.getBoundingBox());

        long time = System.nanoTime();
        boolean skip = !DEPTH_MAP.isAABBVisible(entityVertices, VIEW_MATRIX.last().pose(),
                1 + (float) (entity.position().distanceTo(camera.getPosition()) * 0.03125f));
        preEntityCullingTime += System.nanoTime() - time;
        if (checkCulling)
            skip = !skip;
        if (skip) {
            culledEntity.add(entity);
            entityCulling++;
        } else
            visibleEntity.updateUsageTick(entity, clientTickCount);

        return skip;
    }

    public static ScreenAABB getAABBPoints(AABB aabbIn) {
        Queue<Vec3> queue = Queues.newArrayDeque();
        queue.add(new Vec3(aabbIn.minX, aabbIn.minY, aabbIn.minZ));
        queue.add(new Vec3(aabbIn.maxX, aabbIn.minY, aabbIn.minZ));
        queue.add(new Vec3(aabbIn.minX, aabbIn.maxY, aabbIn.minZ));
        queue.add(new Vec3(aabbIn.minX, aabbIn.minY, aabbIn.maxZ));
        queue.add(new Vec3(aabbIn.maxX, aabbIn.maxY, aabbIn.minZ));
        queue.add(new Vec3(aabbIn.minX, aabbIn.maxY, aabbIn.maxZ));
        queue.add(new Vec3(aabbIn.maxX, aabbIn.minY, aabbIn.maxZ));
        queue.add(new Vec3(aabbIn.maxX, aabbIn.maxY, aabbIn.maxZ));
        Vec3 closest = queue.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(camera.getPosition()))).get();

        return new ScreenAABB(closest, queue);
    }

    public void onProfilerPopPush(String s) {
        if (s.equals("entities")) {
            if (nextLoop) {
                if (!renderingOculus()) {
                    AccessorLevelRender levelFrustum = (AccessorLevelRender) Minecraft.getInstance().levelRenderer;
                    Frustum frustum;
                    if (levelFrustum.getCapturedFrustum() != null) {
                        frustum = levelFrustum.getCapturedFrustum();
                    } else {
                        frustum = levelFrustum.getCullingFrustum();
                    }

                    this.onRender(frustum);
                }
                nextLoop = false;
            } else if (renderingOculus()) {
                AccessorLevelRender levelFrustum = (AccessorLevelRender) Minecraft.getInstance().levelRenderer;
                Frustum frustum;
                if (levelFrustum.getCapturedFrustum() != null) {
                    frustum = levelFrustum.getCapturedFrustum();
                } else {
                    frustum = levelFrustum.getCullingFrustum();
                }

                this.onRender(frustum);
            }

        } else if (s.equals("chunk_graph_rebuild")) {
            if (nextTick) {
                culledChunk.clear();
            }

            chunkCount = 0;
            chunkCulling = 0;
        } else if (s.equals("compilechunks") && applyFrustum) {
            applyFrustum = false;
        } else if (s.equals("clear") && applyFrustum) {
            applyFrustum = false;
        }
    }

    public void onProfilerPush(String s) {
        if (Config.CULL_CHUNK.get() && s.equals("apply_frustum")) {
            applyFrustum = true;

            if (SHADER_LOADER == null || OptiFine != null) {
                if (nextTick) {
                    culledChunk.clear();
                }

                chunkCount = 0;
                chunkCulling = 0;
            }
        } else if (s.equals("center")) {
            camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            int tick = clientTickCount % 20;
            if (nextTick)
                nextTick = false;

            if (this.tick != tick) {
                this.tick = tick;

                nextTick = true;
                nextLoop = true;
            }

            entityCulling = 0;
            entityCount = 0;
            blockCulling = 0;
            blockCount = 0;

            if (nextTick) {
                culledEntity.clear();
                culledBlock.clear();
                if (this.tick == 0) {
                    visibleChunk.tick(clientTickCount, 60);
                    visibleBlock.tick(clientTickCount, 20);
                    visibleEntity.tick(clientTickCount, 20);
                }
                if (tick == 0) {
                    entityCullingTime = preEntityCullingTime;
                    preEntityCullingTime = 0;

                    blockCullingTime = preBlockCullingTime;
                    preBlockCullingTime = 0;

                    initTime = preInitTime;
                    preInitTime = 0;

                    if (preChunkCullingTime != 0) {
                        chunkCullingTime = preChunkCullingTime;
                        preChunkCullingTime = 0;
                    }

                }
            }
        }
    }

    public void onRender(Frustum frustum) {
        if (nextTick) {
            if (CULLING) {
                CullingHandler.FRUSTUM = new Frustum(frustum);
                boolean updateDepth = shouldUpdateDepth();

                if (!checkCulling && updateDepth) {
                    float scale = (float) (double) Config.SAMPLING.get();
                    Window window = Minecraft.getInstance().getWindow();
                    int width = window.getWidth();
                    int height = window.getHeight();

                    int scaleWidth = Math.max(1, (int) (width * scale));
                    int scaleHeight = Math.max(1, (int) (height * scale));
                    if (DEPTH_TARGET.width != scaleWidth || DEPTH_TARGET.height != scaleHeight) {
                        DEPTH_TARGET.resize(scaleWidth, scaleHeight, Minecraft.ON_OSX);
                    }

                    if (SHADER_LOADER != null && SHADER_LOADER.renderingShader()) {
                        RenderSystem.assertOnRenderThreadOrInit();
                        GlStateManager._glBindFramebuffer(36008, SHADER_LOADER.getDepthBuffer());
                        GlStateManager._glBindFramebuffer(36009, DEPTH_TARGET.frameBufferId);
                        GlStateManager._glBlitFrameBuffer(0, 0, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight()
                                , 0, 0, DEPTH_TARGET.width, DEPTH_TARGET.height, 256, 9728);
                        SHADER_LOADER.bindDefaultFrameBuffer();
                    } else
                        DEPTH_TARGET.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

                    DEPTH_TEXTURE = DEPTH_TARGET.getDepthTextureId();
                    long time = System.nanoTime();
                    DEPTH_MAP_STATIC = new DepthMap(scale);
                    DEPTH_MAP = DEPTH_MAP_STATIC;
                    long check = System.nanoTime() - time;
                    preInitTime += check;
                } else if (Config.UPDATE_DEPTH.get() && DEPTH_MAP_STATIC != null) {
                    long time = System.nanoTime();
                    DEPTH_MAP = new DepthMap(DEPTH_MAP_STATIC);
                    preInitTime += System.nanoTime() - time;
                }
            } else
                initTime = 0;
        }

        if (CULLING) {
            if (!checkCulling) {
                net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(Minecraft.getInstance().gameRenderer
                        , camera, Minecraft.getInstance().getFrameTime());
                PoseStack viewMatrix = new PoseStack();
                viewMatrix.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

                viewMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
                viewMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
                Vec3 cameraPos = camera.getPosition();
                viewMatrix.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
                VIEW_MATRIX = viewMatrix;
                PROJECTION_MATRIX = RenderSystem.getProjectionMatrix().copy();
            }
        } else {
            DEPTH_MAP = null;
        }
    }

    public boolean shouldUpdateDepth() {
        return tick == 0;
    }

    public boolean renderingOculus() {
        return SHADER_LOADER != null && OptiFine == null && SHADER_LOADER.renderingShader();
    }

    public boolean nextTick() {
        return nextTick;
    }

    public boolean shouldSkipSetupRender(Camera camera) {
        return Config.CULL_CHUNK.get() && !CullingHandler.INSTANCE.nextTick();
    }
}

package rogo.renderingculling.api;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Checks;
import org.slf4j.Logger;
import rogo.renderingculling.api.data.ChunkCullingMap;
import rogo.renderingculling.api.data.EntityCullingMap;
import rogo.renderingculling.api.impl.IEntitiesForRender;
import rogo.renderingculling.api.impl.IRenderChunkInfo;
import rogo.renderingculling.api.impl.IRenderSectionVisibility;
import rogo.renderingculling.mixin.AccessorLevelRender;
import rogo.renderingculling.mixin.AccessorMinecraft;
import rogo.renderingculling.util.DepthContext;
import rogo.renderingculling.util.LifeTimer;
import rogo.renderingculling.util.NvidiumUtil;
import rogo.renderingculling.util.ShaderLoader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL30.*;

public class CullingHandler {
    public static final String MOD_ID = "brute_force_rendering_culling";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static EntityCullingMap ENTITY_CULLING_MAP = null;
    public static volatile ChunkCullingMap CHUNK_CULLING_MAP = null;
    public static Matrix4f VIEW_MATRIX = new Matrix4f();
    public static Matrix4f PROJECTION_MATRIX = new Matrix4f();
    public static double FOV = 90;

    static {
        PROJECTION_MATRIX.identity();
    }

    public static final int DEPTH_SIZE = 4;
    public static int DEPTH_INDEX;
    public static int MAIN_DEPTH_TEXTURE = 0;
    public static RenderTarget[] DEPTH_BUFFER_TARGET = new RenderTarget[DEPTH_SIZE];
    public static RenderTarget CHUNK_CULLING_MAP_TARGET;
    public static RenderTarget ENTITY_CULLING_MAP_TARGET;
    public static ShaderInstance CHUNK_CULLING_SHADER;
    public static ShaderInstance COPY_DEPTH_SHADER;
    public static ShaderInstance REMOVE_COLOR_SHADER;
    public static ShaderInstance INSTANCED_ENTITY_CULLING_SHADER;
    public static Frustum FRUSTUM;
    public static boolean updatingDepth;
    public static boolean applyFrustum;
    public static int DEBUG = 0;
    public static int[] DEPTH_TEXTURE = new int[DEPTH_SIZE];
    public static ShaderLoader SHADER_LOADER = null;
    public static Class<?> OptiFine = null;

    public static final LifeTimer<Entity> visibleEntity = new LifeTimer<>();
    public static final LifeTimer<BlockPos> visibleBlock = new LifeTimer<>();
    private static boolean[] nextTick = new boolean[20];
    public static int fps = 0;
    private static int tick = 0;
    public static int clientTickCount = 0;
    public static int entityCulling = 0;
    public static int entityCount = 0;
    public static int blockCulling = 0;
    public static int blockCount = 0;
    public static long entityCullingTime = 0;
    public static long blockCullingTime = 0;
    public static long chunkCullingTime = 0;
    private static long preEntityCullingTime = 0;
    private static long preBlockCullingTime = 0;
    private static long preChunkCullingTime = 0;
    public static long preApplyFrustumTime = 0;
    public static long applyFrustumTime = 0;
    public static int chunkCulling = 0;
    public static int chunkCount = 0;
    public static long chunkCullingInitTime = 0;
    public static long preChunkCullingInitTime = 0;
    public static long entityCullingInitTime = 0;
    public static long preEntityCullingInitTime = 0;
    public static int cullingInitCount = 0;
    public static int preCullingInitCount = 0;
    public static boolean checkCulling = false;
    public static boolean checkTexture = false;
    private static boolean usingShader = false;
    private static int fullChunkUpdateCooldown = 0;
    private static String shaderName = "";
    public static int LEVEL_SECTION_RANGE;
    public static int LEVEL_POS_RANGE;
    public static int LEVEL_MIN_SECTION_ABS;
    public static int LEVEL_MIN_POS;
    public static Camera CAMERA;
    private static final HashMap<Integer, Integer> SHADER_DEPTH_BUFFER_ID = new HashMap<>();
    private static int frame;
    private static int lastVisibleUpdatedFrame;
    public static volatile boolean useOcclusionCulling = true;
    public static boolean reColorToolTip = false;

    static {
        RenderSystem.recordRenderCall(() -> {
            for (int i = 0; i < DEPTH_BUFFER_TARGET.length; ++i) {
                DEPTH_BUFFER_TARGET[i] = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false, Minecraft.ON_OSX);
                DEPTH_BUFFER_TARGET[i].setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            }

            CHUNK_CULLING_MAP_TARGET = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false, Minecraft.ON_OSX);
            CHUNK_CULLING_MAP_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            ENTITY_CULLING_MAP_TARGET = new TextureTarget(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), false, Minecraft.ON_OSX);
            ENTITY_CULLING_MAP_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
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

    public static void init() {
        try {
            OptiFine = Class.forName("net.optifine.shaders.Shaders");
        } catch (ClassNotFoundException e) {
            LOGGER.debug("OptiFine Not Found");
        }

        if (OptiFine != null) {
            try {
                SHADER_LOADER = Class.forName("rogo.renderingculling.util.OptiFineLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
            }
        }

        if (hasIris()) {
            try {
                SHADER_LOADER = Class.forName("rogo.renderingculling.util.IrisLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void onWorldUnload(Level world) {
        if (world != Minecraft.getInstance().level) {
            cleanup();
        }
    }

    protected static void cleanup() {
        tick = 0;
        clientTickCount = 0;
        visibleEntity.clear();
        visibleBlock.clear();
        if (CHUNK_CULLING_MAP != null) {
            CHUNK_CULLING_MAP.cleanup();
            CHUNK_CULLING_MAP = null;
        }
        if (ENTITY_CULLING_MAP != null) {
            ENTITY_CULLING_MAP.cleanup();
            ENTITY_CULLING_MAP = null;
        }
        SHADER_DEPTH_BUFFER_ID.clear();
    }

    public static boolean shouldRenderChunk(IRenderSectionVisibility section, boolean count) {
        if (section == null) {
            return false;
        }

        if (Config.getAsyncChunkRebuild()) {
            if (!useOcclusionCulling) {
                return true;
            }

            count = false;
        }

        long time = System.nanoTime();

        if (count)
            chunkCount++;

        boolean render;
        boolean actualRender = false;

        if (!section.shouldCheckVisibility(lastVisibleUpdatedFrame)) {
            render = true;
        } else {
            actualRender = CHUNK_CULLING_MAP.isChunkOffsetCameraVisible(section.getPositionX(), section.getPositionY(), section.getPositionZ());
            render = actualRender;
        }

        if (checkCulling)
            render = !render;

        if (!render && count) {
            chunkCulling++;
        } else if (actualRender) {
            section.updateVisibleTick(lastVisibleUpdatedFrame);
        }

        if (count)
            preChunkCullingTime += System.nanoTime() - time;

        return render;
    }

    public static boolean shouldSkipBlockEntity(BlockEntity blockEntity, AABB aabb, BlockPos pos) {
        blockCount++;

        //for valkyrien skies
        if (CAMERA.getPosition().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) >
                Minecraft.getInstance().options.getEffectiveRenderDistance() * Minecraft.getInstance().options.getEffectiveRenderDistance() * 2) {
            return false;
        }

        if (ENTITY_CULLING_MAP == null || !Config.getCullEntity()) return false;
        if (FRUSTUM == null || !FRUSTUM.isVisible(aabb)) return true;
        String type = BlockEntityType.getKey(blockEntity.getType()).toString();
        if (Config.getBlockEntitiesSkip().contains(type))
            return false;

        long time = System.nanoTime();

        boolean visible;
        boolean actualVisible = false;

        if (visibleBlock.contains(pos)) {
            visible = true;
        } else {
            actualVisible = ENTITY_CULLING_MAP.isObjectVisible(blockEntity);
            visible = actualVisible;
        }

        preBlockCullingTime += System.nanoTime() - time;

        if (checkCulling)
            visible = !visible;

        if (!visible) {
            blockCulling++;
        } else if (actualVisible)
            visibleBlock.updateUsageTick(pos, clientTickCount);

        return !visible;
    }

    public static boolean shouldSkipEntity(Entity entity) {
        entityCount++;
        if (entity instanceof Player || entity.isCurrentlyGlowing()) return false;
        if (entity.distanceToSqr(CAMERA.getPosition()) < 4) return false;
        if (Config.getEntitiesSkip().contains(entity.getType().getDescriptionId()))
            return false;
        if (ENTITY_CULLING_MAP == null || !Config.getCullEntity()) return false;

        long time = System.nanoTime();

        boolean visible;
        boolean actualVisible = false;

        if (visibleEntity.contains(entity)) {
            visible = true;
        } else {
            actualVisible = ENTITY_CULLING_MAP.isObjectVisible(entity);
            visible = actualVisible;
        }

        preEntityCullingTime += System.nanoTime() - time;

        if (checkCulling)
            visible = !visible;

        if (!visible) {
            entityCulling++;
        } else if (actualVisible)
            visibleEntity.updateUsageTick(entity, clientTickCount);

        return !visible;
    }

    public static void onProfilerPopPush(String s) {
        switch (s) {
            case "beforeRunTick" -> {
                if (((AccessorLevelRender) Minecraft.getInstance().levelRenderer).getNeedsFullRenderChunkUpdate() && Minecraft.getInstance().level != null) {
                    fullChunkUpdateCooldown = 20;

                    LEVEL_SECTION_RANGE = Minecraft.getInstance().level.getMaxSection() - Minecraft.getInstance().level.getMinSection();
                    LEVEL_MIN_SECTION_ABS = Math.abs(Minecraft.getInstance().level.getMinSection());
                    LEVEL_MIN_POS = Minecraft.getInstance().level.getMinBuildHeight();
                    LEVEL_POS_RANGE = Minecraft.getInstance().level.getMaxBuildHeight() - Minecraft.getInstance().level.getMinBuildHeight();
                }
            }
            case "afterRunTick" -> {
                ++frame;
                updateMapData();
            }
            case "captureFrustum" -> {
                AccessorLevelRender levelFrustum = (AccessorLevelRender) Minecraft.getInstance().levelRenderer;
                Frustum frustum;
                if (levelFrustum.getCapturedFrustum() != null) {
                    frustum = levelFrustum.getCapturedFrustum();
                } else {
                    frustum = levelFrustum.getCullingFrustum();
                }
                CullingHandler.FRUSTUM = new Frustum(frustum).offsetToFullyIncludeCameraCube(32);
                if (CullingHandler.CHUNK_CULLING_MAP != null) {
                    CullingHandler.CHUNK_CULLING_MAP.updateCamera();
                }
                checkShader();
            }
            case "terrain_setup" -> {
                applyFrustum = true;
            }
            case "compilechunks" -> {
                applyFrustum = false;
            }
            case "destroyProgress" -> {
                updatingDepth = true;
                updateDepthMap();
                readMapData();
                CullingRenderEvent.updateCullingMap();
                updatingDepth = false;
            }
            case "chunk_render_lists" -> {
                chunkCount = 0;
                chunkCulling = 0;
            }
        }
    }

    public static void onProfilerPush(String s) {
        if(s.equals("onKeyboardInput")) {
            ModLoader.onKeyPress();
        } if (Config.shouldCullChunk() && s.equals("apply_frustum")) {
            if (SHADER_LOADER == null || OptiFine != null) {
                chunkCount = 0;
                chunkCulling = 0;
            }
        } else if (s.equals("center")) {
            CAMERA = Minecraft.getInstance().gameRenderer.getMainCamera();
            int thisTick = clientTickCount % 20;
            nextTick = new boolean[20];

            if (tick != thisTick) {
                tick = thisTick;
                nextTick[thisTick] = true;
            }

            entityCulling = 0;
            entityCount = 0;
            blockCulling = 0;
            blockCount = 0;

            if (anyNextTick() && fullChunkUpdateCooldown > 0) {
                fullChunkUpdateCooldown--;
            }

            if (isNextLoop()) {
                visibleBlock.tick(clientTickCount, 1);
                visibleEntity.tick(clientTickCount, 1);

                applyFrustumTime = preApplyFrustumTime;
                preApplyFrustumTime = 0;

                entityCullingTime = preEntityCullingTime;
                preEntityCullingTime = 0;

                blockCullingTime = preBlockCullingTime;
                preBlockCullingTime = 0;

                chunkCullingInitTime = preChunkCullingInitTime;
                preChunkCullingInitTime = 0;

                cullingInitCount = preCullingInitCount;
                preCullingInitCount = 0;

                entityCullingInitTime = preEntityCullingInitTime;
                preEntityCullingInitTime = 0;

                if (CullingHandler.CHUNK_CULLING_MAP != null) {
                    CullingHandler.CHUNK_CULLING_MAP.lastQueueUpdateCount = CullingHandler.CHUNK_CULLING_MAP.queueUpdateCount;
                    CullingHandler.CHUNK_CULLING_MAP.queueUpdateCount = 0;
                }

                if (preChunkCullingTime != 0) {
                    chunkCullingTime = preChunkCullingTime;
                    preChunkCullingTime = 0;
                }
            }
        }
    }

    public static void readMapData() {
        if (!checkCulling) {
            if (Config.getCullChunk()) {
                long time = System.nanoTime();
                if (CHUNK_CULLING_MAP != null && CHUNK_CULLING_MAP.isTransferred()) {
                    CHUNK_CULLING_MAP.readData();
                    lastVisibleUpdatedFrame = frame;
                }
                preChunkCullingInitTime += System.nanoTime() - time;
            }

            if (Config.getCullEntity()) {
                long time = System.nanoTime();
                if (ENTITY_CULLING_MAP != null && ENTITY_CULLING_MAP.isTransferred()) {
                    ENTITY_CULLING_MAP.readData();
                    lastVisibleUpdatedFrame = frame;
                }
                preEntityCullingInitTime += System.nanoTime() - time;
            }
        }
    }

    public static void checkShader() {
        if (SHADER_LOADER != null) {
            boolean clear = false;
            if (SHADER_LOADER.enabledShader() && !usingShader) {
                clear = true;
                usingShader = true;
            }

            if (!SHADER_LOADER.enabledShader() && usingShader) {
                clear = true;
                usingShader = false;
            }

            if (SHADER_LOADER.enabledShader() && OptiFine != null) {
                String shaderPack = "";
                try {
                    Field field = CullingHandler.OptiFine.getDeclaredField("currentShaderName");
                    field.setAccessible(true);
                    shaderPack = (String) field.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.fillInStackTrace();
                }
                if (!Objects.equals(shaderName, shaderPack)) {
                    shaderName = shaderPack;
                    clear = true;
                }
            }

            if (clear) {
                cleanup();
            }
        }
    }

    public static void updateDepthMap() {
        CullingHandler.PROJECTION_MATRIX = new Matrix4f(RenderSystem.getProjectionMatrix());
        if (anyCulling() && !checkCulling && anyNeedTransfer()) {
            float sampling = (float) (double) Config.getSampling();
            Window window = Minecraft.getInstance().getWindow();
            int width = window.getWidth();
            int height = window.getHeight();

            runOnDepthFrame((depthContext) -> {
                int scaleWidth = Math.max(1, (int) (width * sampling * depthContext.scale()));
                int scaleHeight = Math.max(1, (int) (height * sampling * depthContext.scale()));
                if (depthContext.frame().width != scaleWidth || depthContext.frame().height != scaleHeight) {
                    depthContext.frame().resize(scaleWidth, scaleHeight, Minecraft.ON_OSX);
                }
            });

            int depthTexture = Minecraft.getInstance().getMainRenderTarget().getDepthTextureId();
            if (SHADER_LOADER != null && SHADER_LOADER.enabledShader()) {
                if (!SHADER_DEPTH_BUFFER_ID.containsKey(SHADER_LOADER.getFrameBufferID())) {
                    RenderSystem.assertOnRenderThreadOrInit();
                    GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, SHADER_LOADER.getFrameBufferID());

                    int attachmentType = GL_DEPTH_ATTACHMENT;
                    int[] attachmentObjectType = new int[1];
                    glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, attachmentType, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, attachmentObjectType);

                    if (attachmentObjectType[0] == GL_TEXTURE) {
                        int[] depthTextureID = new int[1];
                        glGetFramebufferAttachmentParameteriv(GL_FRAMEBUFFER, attachmentType, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, depthTextureID);
                        depthTexture = depthTextureID[0];
                        SHADER_DEPTH_BUFFER_ID.put(SHADER_LOADER.getFrameBufferID(), depthTexture);
                    }
                } else {
                    depthTexture = SHADER_DEPTH_BUFFER_ID.get(SHADER_LOADER.getFrameBufferID());
                }
            }

            MAIN_DEPTH_TEXTURE = depthTexture;

            runOnDepthFrame((depthContext) -> {
                useShader(CullingHandler.COPY_DEPTH_SHADER);
                depthContext.frame().clear(Minecraft.ON_OSX);
                depthContext.frame().bindWrite(false);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
                bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
                bufferbuilder.vertex(1.0f, 1.0f, 0.0f).endVertex();
                bufferbuilder.vertex(-1.0f, 1.0f, 0.0f).endVertex();
                RenderSystem.setShaderTexture(0, depthContext.lastTexture());
                tesselator.end();
                DEPTH_TEXTURE[depthContext.index()] = depthContext.frame().getColorTextureId();
            });

            bindMainFrameTarget();
            net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(Minecraft.getInstance().gameRenderer
                    , CAMERA, Minecraft.getInstance().getFrameTime());
            PoseStack viewMatrix = new PoseStack();
            Vec3 cameraPos = CAMERA.getPosition();
            viewMatrix.mulPose(Axis.ZP.rotationDegrees(cameraSetup.getRoll()));
            viewMatrix.mulPose(Axis.XP.rotationDegrees(CAMERA.getXRot()));
            viewMatrix.mulPose(Axis.YP.rotationDegrees(CAMERA.getYRot() + 180.0F));
            viewMatrix.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
            VIEW_MATRIX = new Matrix4f(viewMatrix.last().pose());
        }
    }

    public static void updateMapData() {
        if (anyCulling()) {
            preCullingInitCount++;

            if (Config.getCullChunk()) {
                int renderingDiameter = Minecraft.getInstance().options.getEffectiveRenderDistance() * 2 + 1;
                int maxSize = renderingDiameter * LEVEL_SECTION_RANGE * renderingDiameter;
                int cullingSize = (int) Math.sqrt(maxSize) + 1;

                if (CHUNK_CULLING_MAP_TARGET.width != cullingSize || CHUNK_CULLING_MAP_TARGET.height != cullingSize) {
                    CHUNK_CULLING_MAP_TARGET.resize(cullingSize, cullingSize, Minecraft.ON_OSX);
                    if (CHUNK_CULLING_MAP != null) {
                        CHUNK_CULLING_MAP.cleanup();
                        CHUNK_CULLING_MAP = new ChunkCullingMap(CHUNK_CULLING_MAP_TARGET.width, CHUNK_CULLING_MAP_TARGET.height);
                        CHUNK_CULLING_MAP.generateIndex(Minecraft.getInstance().options.getEffectiveRenderDistance());
                    }
                }

                if (CHUNK_CULLING_MAP == null) {
                    CHUNK_CULLING_MAP = new ChunkCullingMap(CHUNK_CULLING_MAP_TARGET.width, CHUNK_CULLING_MAP_TARGET.height);
                    if (ENTITY_CULLING_MAP != null) {
                        ENTITY_CULLING_MAP.syncDelay(CHUNK_CULLING_MAP);
                    }
                    CHUNK_CULLING_MAP.generateIndex(Minecraft.getInstance().options.getEffectiveRenderDistance());
                }

                long time = System.nanoTime();
                CHUNK_CULLING_MAP.transferData();
                preChunkCullingInitTime += System.nanoTime() - time;
            }

            if (Config.getCullEntity()) {
                if (ENTITY_CULLING_MAP == null) {
                    ENTITY_CULLING_MAP = new EntityCullingMap(ENTITY_CULLING_MAP_TARGET.width, ENTITY_CULLING_MAP_TARGET.height);
                    if (CHUNK_CULLING_MAP != null) {
                        CHUNK_CULLING_MAP.syncDelay(ENTITY_CULLING_MAP);
                    }
                }

                int tableCapacity = CullingHandler.ENTITY_CULLING_MAP.getEntityTable().size() / 64;
                tableCapacity = tableCapacity * 64 + 64;
                int cullingSize = (int) Math.sqrt(tableCapacity) + 1;
                if (CullingHandler.ENTITY_CULLING_MAP_TARGET.width != cullingSize || CullingHandler.ENTITY_CULLING_MAP_TARGET.height != cullingSize) {
                    CullingHandler.ENTITY_CULLING_MAP_TARGET.resize(cullingSize, cullingSize, Minecraft.ON_OSX);
                    if (ENTITY_CULLING_MAP != null) {
                        EntityCullingMap temp = ENTITY_CULLING_MAP;
                        ENTITY_CULLING_MAP = new EntityCullingMap(ENTITY_CULLING_MAP_TARGET.width, ENTITY_CULLING_MAP_TARGET.height);
                        ENTITY_CULLING_MAP.getEntityTable().copyTemp(temp.getEntityTable(), clientTickCount);
                        temp.cleanup();
                    }
                }

                long time = System.nanoTime();
                ENTITY_CULLING_MAP.transferData();
                preEntityCullingInitTime += System.nanoTime() - time;

                if (Minecraft.getInstance().level != null) {
                    CullingHandler.ENTITY_CULLING_MAP.getEntityTable().tick(clientTickCount);
                    Iterable<Entity> entities = Minecraft.getInstance().level.entitiesForRendering();
                    entities.forEach(entity -> CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addObject(entity));
                    for (Object levelrenderer$renderchunkinfo : ((IEntitiesForRender) Minecraft.getInstance().levelRenderer).renderChunksInFrustum()) {
                        List<BlockEntity> list = ((IRenderChunkInfo) levelrenderer$renderchunkinfo).getRenderChunk().getCompiledChunk().getRenderableBlockEntities();
                        list.forEach(entity -> CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addObject(entity));
                    }

                    CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addAllTemp();
                }
            }

            fps = ((AccessorMinecraft) Minecraft.getInstance()).getFps();
        } else {
            if (ENTITY_CULLING_MAP != null) {
                ENTITY_CULLING_MAP.cleanup();
                ENTITY_CULLING_MAP = null;
            }
            if (CHUNK_CULLING_MAP != null) {
                CHUNK_CULLING_MAP.cleanup();
                CHUNK_CULLING_MAP = null;
            }
        }
    }

    public static void useShader(ShaderInstance instance) {
        RenderSystem.setShader(() -> instance);
    }

    public static void bindMainFrameTarget() {
        if (SHADER_LOADER != null && SHADER_LOADER.renderingShaderPass()) {
            SHADER_LOADER.bindDefaultFrameBuffer();
        } else {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    public static void runOnDepthFrame(Consumer<DepthContext> consumer) {
        float f = 1.0f;
        for (DEPTH_INDEX = 0; DEPTH_INDEX < DEPTH_BUFFER_TARGET.length; ++DEPTH_INDEX) {
            int lastTexture = DEPTH_INDEX == 0 ? MAIN_DEPTH_TEXTURE : DEPTH_BUFFER_TARGET[DEPTH_INDEX - 1].getColorTextureId();
            consumer.accept(new DepthContext(DEPTH_BUFFER_TARGET[DEPTH_INDEX], DEPTH_INDEX, f, lastTexture));
            f *= 0.3f;
        }
    }

    public static void callDepthTexture() {
        CullingHandler.runOnDepthFrame((depthContext) -> {
            RenderSystem.setShaderTexture(depthContext.index(), CullingHandler.DEPTH_TEXTURE[depthContext.index()]);
        });
    }

    public static boolean renderingIris() {
        return renderingShader() && OptiFine == null;
    }

    public static boolean renderingShader() {
        return SHADER_LOADER != null && SHADER_LOADER.renderingShaderPass();
    }

    public static boolean enabledShader() {
        return SHADER_LOADER != null && SHADER_LOADER.enabledShader();
    }

    public static boolean anyNextTick() {
        for (int i = 0; i < 20; ++i) {
            if (nextTick[i])
                return true;
        }
        return false;
    }

    public static boolean isNextLoop() {
        return nextTick[0];
    }

    public static boolean anyCulling() {
        return Config.getCullChunk() || Config.getCullEntity();
    }

    public static boolean anyNeedTransfer() {
        return (CullingHandler.ENTITY_CULLING_MAP != null && CullingHandler.ENTITY_CULLING_MAP.needTransferData()) ||
                (CullingHandler.CHUNK_CULLING_MAP != null && CullingHandler.CHUNK_CULLING_MAP.needTransferData());
    }

    private static int gl33 = -1;

    public static boolean gl33() {
        if (RenderSystem.isOnRenderThread()) {
            if (gl33 < 0)
                gl33 = (GL.getCapabilities().OpenGL33 || Checks.checkFunctions(GL.getCapabilities().glVertexAttribDivisor)) ? 1 : 0;
        }
        return gl33 == 1;
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

    public static boolean needPauseRebuild() {
        return fullChunkUpdateCooldown > 0;
    }

    public static int mapChunkY(double posY) {
        double offset = posY - LEVEL_MIN_POS;
        double mappingRatio = offset / LEVEL_POS_RANGE;

        return (int) Math.floor(mappingRatio * LEVEL_SECTION_RANGE);
    }
}

package rogo.renderingculling.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import rogo.renderingculling.gui.ConfigScreen;
import rogo.renderingculling.mixin.AccessorLevelRender;
import rogo.renderingculling.mixin.AccessorMinecraft;
import rogo.renderingculling.util.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME;

public class CullingHandler implements ModInitializer {
    public static CullingHandler INSTANCE;
    public static final String MOD_ID = "brute_force_rendering_culling";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static EntityCullingMap ENTITY_CULLING_MAP = null;
    public static ChunkCullingMap CHUNK_CULLING_MAP = null;
    public static Matrix4f VIEW_MATRIX = new Matrix4f();
    public static Matrix4f PROJECTION_MATRIX = new Matrix4f();

    static {
        PROJECTION_MATRIX.loadIdentity();
    }

    public static RenderTarget DEPTH_BUFFER_TARGET;
    public static RenderTarget MONOCHROME_DEPTH_TARGET;
    public static RenderTarget CHUNK_CULLING_MAP_TARGET;
    public static RenderTarget ENTITY_CULLING_MAP_TARGET;
    public static ShaderInstance CHUNK_CULLING_SHADER;
    public static ShaderInstance LINEARIZE_DEPTH_SHADER;
    public static ShaderInstance COPY_DEPTH_SHADER;
    public static ShaderInstance INSTANCED_ENTITY_CULLING_SHADER;
    public static Frustum FRUSTUM;
    public static boolean updatingDepth;
    public static boolean applyFrustum;
    public boolean DEBUG = false;
    public static int DEPTH_TEXTURE;
    public static ShaderLoader SHADER_LOADER = null;
    public static Class<?> OptiFine = null;

    public LifeTimer<Entity> visibleEntity = new LifeTimer<>();
    public LifeTimer<BlockPos> visibleBlock = new LifeTimer<>();
    public LifeTimer<BlockPos> visibleChunk = new LifeTimer<>();
    public HashSet<Entity> culledEntity = new HashSet<>();
    public HashSet<BlockPos> culledBlock = new HashSet<>();
    private boolean[] nextTick = new boolean[20];
    public int fps = 0;
    private int tick = 0;
    public int clientTickCount = 0;
    public int entityCulling = 0;
    public int entityCount = 0;
    public int blockCulling = 0;
    public int blockCount = 0;
    public long entityCullingTime = 0;
    public long blockCullingTime = 0;
    public long chunkCullingTime = 0;
    private long preEntityCullingTime = 0;
    private long preBlockCullingTime = 0;
    private long preChunkCullingTime = 0;
    public long preApplyFrustumTime = 0;
    public long applyFrustumTime = 0;
    public int chunkCulling = 0;
    public int chunkCount = 0;
    public long chunkCullingInitTime = 0;
    public long preChunkCullingInitTime = 0;
    public long entityCullingInitTime = 0;
    public long preEntityCullingInitTime = 0;
    public int cullingInitCount = 0;
    public int preCullingInitCount = 0;
    public boolean checkCulling = false;
    private boolean usingShader = false;
    private String shaderName = "";
    protected static int LEVEL_HEIGHT_OFFSET;
    protected static int LEVEL_MIN_SECTION_ABS;
    public static Camera camera;
    private static final HashMap<Integer, Integer> SHADER_DEPTH_BUFFER_ID = new HashMap<>();

    static {
        RenderSystem.recordRenderCall(() -> {
            DEPTH_BUFFER_TARGET = new TextureTarget(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false, MinecraftClient.ON_OSX);
            DEPTH_BUFFER_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            CHUNK_CULLING_MAP_TARGET = new TextureTarget(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false, MinecraftClient.ON_OSX);
            CHUNK_CULLING_MAP_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            MONOCHROME_DEPTH_TARGET = new TextureTarget(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false, MinecraftClient.ON_OSX);
            MONOCHROME_DEPTH_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            ENTITY_CULLING_MAP_TARGET = new TextureTarget(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false, MinecraftClient.ON_OSX);
            ENTITY_CULLING_MAP_TARGET.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        });
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            MinecraftForge.EVENT_BUS.register(new CullingRenderEvent());
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
                    SHADER_LOADER = Class.forName("rogo.renderingculling.util.OptiFineLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
                }
            }

            if (FMLLoader.getLoadingModList().getMods().stream().anyMatch(modInfo -> modInfo.getModId().equals("oculus"))) {
                try {
                    SHADER_LOADER = Class.forName("rogo.renderingculling.util.IrisLoaderImpl").asSubclass(ShaderLoader.class).newInstance();
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
            CHUNK_CULLING_SHADER = new ShaderInstance(MinecraftClient.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "chunk_culling"), DefaultVertexFormat.POSITION);
            LINEARIZE_DEPTH_SHADER = new ShaderInstance(MinecraftClient.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "linearize_depth"), DefaultVertexFormat.POSITION);
            INSTANCED_ENTITY_CULLING_SHADER = new ShaderInstance(MinecraftClient.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "instanced_entity_culling"), DefaultVertexFormat.POSITION);
            COPY_DEPTH_SHADER = new ShaderInstance(MinecraftClient.getInstance().getResourceManager(), new ResourceLocation(MOD_ID, "copy_depth"), DefaultVertexFormat.POSITION);
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

    @SubscribeEvent
    public void onClientTick(WorldEvent.Unload event) {
        if(event.getWorld() == MinecraftClient.getInstance().level) {
            cleanup();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().level != null) {
                clientTickCount++;
                if (clientTickCount > 200 && CHUNK_CULLING_MAP != null && !CHUNK_CULLING_MAP.isDone()) {
                    CHUNK_CULLING_MAP.setDone();
                    LEVEL_HEIGHT_OFFSET = MinecraftClient.getInstance().level.getMaxSection() - MinecraftClient.getInstance().level.getMinSection();
                    LEVEL_MIN_SECTION_ABS = Math.abs(MinecraftClient.getInstance().level.getMinSection());
                }
            } else {
                cleanup();
            }
        }
    }

    private void cleanup() {
        this.tick = 0;
        clientTickCount = 0;
        visibleChunk.clear();
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

    @SubscribeEvent
    public void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (MinecraftClient.getInstance().player != null) {
            if (CONFIG_KEY.isDown()) {
                MinecraftClient.getInstance().setScreen(new ConfigScreen(new TranslatableComponent(MOD_ID + ".config")));
            }
            if (DEBUG_KEY.isDown()) {
                DEBUG = !DEBUG;
            }
        }
    }

    public boolean shouldRenderChunk(AABB aabb) {
        chunkCount++;
        if (!Config.CULL_CHUNK.get() || CHUNK_CULLING_MAP == null || !CHUNK_CULLING_MAP.isDone()) {
            return true;
        }
        BlockPos pos = new BlockPos(aabb.getCenter());
        boolean render;
        boolean actualRender = false;
        long time = System.nanoTime();

        if (visibleChunk.contains(pos)) {
            render = true;
        } else {
            actualRender = CHUNK_CULLING_MAP.isChunkVisible(pos);
            render = actualRender;
        }

        preChunkCullingTime += System.nanoTime() - time;

        if (checkCulling)
            render = !render;

        if (!render) {
            chunkCulling++;
        } else if(actualRender) {
            visibleChunk.updateUsageTick(pos, clientTickCount);
        }

        return render;
    }

    public boolean shouldSkipBlock(BlockEntity blockEntity, AABB aabb, BlockPos pos) {
        blockCount++;
        if (ENTITY_CULLING_MAP == null || !Config.CULL_ENTITY.get()) return false;
        if (FRUSTUM == null || !FRUSTUM.isVisible(aabb)) return true;
        String type = BlockEntityType.getKey(blockEntity.getType()).toString();
        if (Config.BLOCK_ENTITY_SKIP.get().contains(type))
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
            culledBlock.add(pos);
            blockCulling++;
        } else if(actualVisible)
            visibleBlock.updateUsageTick(pos, clientTickCount);

        return !visible;
    }

    public boolean shouldSkipEntity(Entity entity) {
        entityCount++;
        if (entity instanceof Player || entity.isCurrentlyGlowing()) return false;
        if (entity.distanceToSqr(camera.getPosition()) < 4) return false;
        if (Config.ENTITY_SKIP.get().contains(entity.getType().getRegistryName().toString()))
            return false;
        if (ENTITY_CULLING_MAP == null || !Config.CULL_ENTITY.get()) return false;

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
            culledEntity.add(entity);
            entityCulling++;
        } else if(actualVisible)
            visibleEntity.updateUsageTick(entity, clientTickCount);

        return !visible;
    }

    public void onProfilerPopPush(String s) {
        if (s.equals("afterRunTick")) {
            afterGameRender();
        } else if (s.equals("captureFrustum")) {
            AccessorLevelRender levelFrustum = (AccessorLevelRender) MinecraftClient.getInstance().levelRenderer;
            Frustum frustum;
            if (levelFrustum.getCapturedFrustum() != null) {
                frustum = levelFrustum.getCapturedFrustum();
            } else {
                frustum = levelFrustum.getCullingFrustum();
            }
            CullingHandler.FRUSTUM = new Frustum(frustum).offsetToFullyIncludeCameraCube(32);
            this.beforeRenderingWorld();
        } else if (s.equals("terrain_setup")) {
            applyFrustum = true;
        }  else if (s.equals("compilechunks")) {
            applyFrustum = false;
        } else if (s.equals("destroyProgress")) {
            updatingDepth = true;
            this.afterRenderingWorld();
            CullingRenderEvent.onUpdateCullingMap();
            updatingDepth = false;
        } else if (s.equals("chunk_graph_rebuild")) {
            chunkCount = 0;
            chunkCulling = 0;
        }
    }

    public void onProfilerPush(String s) {
        if (Config.CULL_CHUNK.get() && s.equals("apply_frustum")) {
            if (SHADER_LOADER == null || OptiFine != null) {
                chunkCount = 0;
                chunkCulling = 0;
            }
        } else if (s.equals("center")) {
            camera = MinecraftClient.getInstance().gameRenderer.getMainCamera();
            int tick = clientTickCount % 20;
            nextTick = new boolean[20];

            if (this.tick != tick) {
                this.tick = tick;
                nextTick[tick] = true;
            }

            entityCulling = 0;
            entityCount = 0;
            blockCulling = 0;
            blockCount = 0;

            if (isNextLoop()) {
                visibleChunk.tick(clientTickCount, 1);
                visibleBlock.tick(clientTickCount, 1);
                visibleEntity.tick(clientTickCount, 1);
                if (tick == 0) {
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


                    if (preChunkCullingTime != 0) {
                        chunkCullingTime = preChunkCullingTime;
                        preChunkCullingTime = 0;
                    }
                }
            }
        }
    }

    public void beforeRenderingWorld() {
        if(SHADER_LOADER != null) {
            boolean clear = false;
            if(SHADER_LOADER.renderingShader() && !usingShader) {
                clear = true;
                usingShader = true;
            }

            if(!SHADER_LOADER.renderingShader() && usingShader) {
                clear = true;
                usingShader = false;
            }

            if(SHADER_LOADER.renderingShader() && OptiFine != null) {
                String shaderPack = "";
                try {
                    Field field = CullingHandler.OptiFine.getDeclaredField("currentShaderName");
                    field.setAccessible(true);
                    shaderPack = (String) field.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.fillInStackTrace();
                }
                if(!Objects.equals(shaderName, shaderPack)) {
                    shaderName = shaderPack;
                    clear = true;
                }
            }

            if(clear)
                cleanup();
        }

        if (anyCulling()) {
            if (!checkCulling) {
                if(Config.CULL_CHUNK.get()) {
                    long time = System.nanoTime();
                    if (Config.CULL_CHUNK.get() && CHUNK_CULLING_MAP != null && CHUNK_CULLING_MAP.isTransferred()) {
                        CHUNK_CULLING_MAP.readData();
                    }
                    preChunkCullingInitTime += System.nanoTime() - time;
                }

                if(Config.CULL_ENTITY.get()) {
                    long time = System.nanoTime();
                    if (ENTITY_CULLING_MAP != null && ENTITY_CULLING_MAP.isTransferred()) {
                        ENTITY_CULLING_MAP.readData();
                    }
                    preEntityCullingInitTime += System.nanoTime() - time;
                }
            }
        }
    }

    public void afterRenderingWorld() {
        if (anyCulling() && !checkCulling) {
            float scale = (float) (double) Config.SAMPLING.get();
            Window window = MinecraftClient.getInstance().getWindow();
            int width = window.getWidth();
            int height = window.getHeight();

            int scaleWidth = Math.max(1, (int) (width * scale));
            int scaleHeight = Math.max(1, (int) (height * scale));
            if (DEPTH_BUFFER_TARGET.width != scaleWidth || DEPTH_BUFFER_TARGET.height != scaleHeight) {
                DEPTH_BUFFER_TARGET.resize(scaleWidth, scaleHeight, MinecraftClient.ON_OSX);
            }

            int depthTexture = MinecraftClient.getInstance().getMainRenderTarget().getDepthTextureId();
            if (SHADER_LOADER != null && SHADER_LOADER.renderingShader()) {
                if(!SHADER_DEPTH_BUFFER_ID.containsKey(SHADER_LOADER.getFrameBufferID())) {
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

            useShader(CullingHandler.COPY_DEPTH_SHADER);
            CullingHandler.DEPTH_BUFFER_TARGET.clear(MinecraftClient.ON_OSX);
            CullingHandler.DEPTH_BUFFER_TARGET.bindWrite(false);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(-1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f, -1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(1.0f,  1.0f, 0.0f).endVertex();
            bufferbuilder.vertex(-1.0f,  1.0f, 0.0f).endVertex();
            RenderSystem.setShaderTexture(0, depthTexture);
            tesselator.end();
            DEPTH_TEXTURE = DEPTH_BUFFER_TARGET.getColorTextureId();

            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(MinecraftClient.getInstance().gameRenderer
                    , camera, MinecraftClient.getInstance().getFrameTime());
            PoseStack viewMatrix = new PoseStack();
            viewMatrix.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));
            viewMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
            viewMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
            Vec3 cameraPos = camera.getPosition();
            viewMatrix.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
            VIEW_MATRIX = viewMatrix.last().pose().copy();
        }
    }

    public void afterGameRender() {
        if (anyCulling()) {
            preCullingInitCount++;

            if(Config.CULL_CHUNK.get()) {
                int renderingDiameter = MinecraftClient.getInstance().options.getEffectiveRenderDistance() * 2 + 1;
                int maxSize = renderingDiameter * LEVEL_HEIGHT_OFFSET * renderingDiameter;
                int cullingSize = (int) Math.sqrt(maxSize) + 1;

                if (CHUNK_CULLING_MAP_TARGET.width != cullingSize || CHUNK_CULLING_MAP_TARGET.height != cullingSize) {
                    CHUNK_CULLING_MAP_TARGET.resize(cullingSize, cullingSize, MinecraftClient.ON_OSX);
                    if (CHUNK_CULLING_MAP != null) {
                        CHUNK_CULLING_MAP.cleanup();
                        CHUNK_CULLING_MAP = new ChunkCullingMap(CHUNK_CULLING_MAP_TARGET.width, CHUNK_CULLING_MAP_TARGET.height);
                        CHUNK_CULLING_MAP.generateIndex(MinecraftClient.getInstance().options.getEffectiveRenderDistance());
                    }
                }

                if (CHUNK_CULLING_MAP == null) {
                    CHUNK_CULLING_MAP = new ChunkCullingMap(CHUNK_CULLING_MAP_TARGET.width, CHUNK_CULLING_MAP_TARGET.height);
                    CHUNK_CULLING_MAP.generateIndex(MinecraftClient.getInstance().options.getEffectiveRenderDistance());
                }

                long time = System.nanoTime();
                CHUNK_CULLING_MAP.transferData();
                preChunkCullingInitTime += System.nanoTime() - time;
            }

            if(Config.CULL_ENTITY.get()) {
                if (ENTITY_CULLING_MAP == null) {
                    ENTITY_CULLING_MAP = new EntityCullingMap(ENTITY_CULLING_MAP_TARGET.width, ENTITY_CULLING_MAP_TARGET.height);
                }

                int tableCapacity = CullingHandler.ENTITY_CULLING_MAP.getEntityTable().size()/64;
                tableCapacity = tableCapacity*64+64;
                int cullingSize = (int) Math.sqrt(tableCapacity)+1;
                if(CullingHandler.ENTITY_CULLING_MAP_TARGET.width != cullingSize || CullingHandler.ENTITY_CULLING_MAP_TARGET.height != cullingSize) {
                    CullingHandler.ENTITY_CULLING_MAP_TARGET.resize(cullingSize, cullingSize, MinecraftClient.ON_OSX);
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

                if(MinecraftClient.getInstance().level != null) {
                    CullingHandler.ENTITY_CULLING_MAP.getEntityTable().tick(clientTickCount);
                    Iterable<Entity> entities = MinecraftClient.getInstance().level.entitiesForRendering();
                    entities.forEach(entity -> CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addObject(entity));
                    for(Object levelrenderer$renderchunkinfo : ((IEntitiesForRender)MinecraftClient.getInstance().levelRenderer).renderChunksInFrustum()) {
                        List<BlockEntity> list = ((IRenderChunkInfo)levelrenderer$renderchunkinfo).getRenderChunk().getCompiledChunk().getRenderableBlockEntities();
                        list.forEach(entity -> CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addObject(entity));
                    }

                    CullingHandler.ENTITY_CULLING_MAP.getEntityTable().addAllTemp();
                }
            }

            fps = ((AccessorMinecraft) MinecraftClient.getInstance()).getFps();
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
        RenderSystem.setShader(()-> instance);
    }

    public static void bindMainFrameTarget() {
        if(SHADER_LOADER != null && SHADER_LOADER.renderingShader()) {
            SHADER_LOADER.bindDefaultFrameBuffer();
        } else {
            MinecraftClient.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    public boolean renderingOculus() {
        return SHADER_LOADER != null && OptiFine == null && SHADER_LOADER.renderingShader();
    }

    public boolean isNextTick(int tick) {
        return nextTick[this.tick];
    }

    public boolean anyNextTick() {
        for (int i = 0; i < 20; ++i) {
            if (nextTick[i])
                return true;
        }
        return false;
    }

    public boolean isNextLoop() {
        return nextTick[0];
    }

    public static boolean anyCulling() {
        return Config.CULL_ENTITY.get() || Config.CULL_CHUNK.get();
    }
}

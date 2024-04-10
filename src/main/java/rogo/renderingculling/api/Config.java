package rogo.renderingculling.api;

import com.google.common.collect.ImmutableList;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final PropertyMirror<Double> SAMPLING = PropertyMirror.create(ConfigTypes.DOUBLE);

    public static double getSampling() {
        if(unload())
            return 0.2;

        return SAMPLING.getValue();
    }

    public static void setSampling(double value) {
        SAMPLING.setValue(value);
    }

    private static final PropertyMirror<Boolean> CULL_ENTITY = PropertyMirror.create(ConfigTypes.BOOLEAN);

    public static boolean getCullEntity() {
        if(unload() || !CullingHandler.gl33())
            return false;
        return CULL_ENTITY.getValue();
    }

    public static void setCullEntity(boolean value) {
        CULL_ENTITY.setValue(value);
    }

    private static final PropertyMirror<Boolean> CULL_CHUNK = PropertyMirror.create(ConfigTypes.BOOLEAN);

    public static boolean getCullChunk() {
        if(unload())
            return false;
        return CULL_CHUNK.getValue();
    }

    public static void setCullChunk(boolean value) {
        CULL_CHUNK.setValue(value);
    }

    private static final PropertyMirror<Integer> UPDATE_DELAY = PropertyMirror.create(ConfigTypes.INTEGER);

    public static int getDepthUpdateDelay() {
        if(unload())
            return 1;
        return UPDATE_DELAY.getValue();
    }

    public static void setDepthUpdateDelay(int value) {
        UPDATE_DELAY.setValue(value);
    }

    private static final PropertyMirror<Integer> CULLING_ENTITY_RATE = PropertyMirror.create(ConfigTypes.INTEGER);

    public static int getCullingEntityRate() {
        if(unload())
            return 20;
        return CULLING_ENTITY_RATE.getValue();
    }

    public static void setCullingEntityRate(int value) {
        CULLING_ENTITY_RATE.setValue(value);
    }

    private static final PropertyMirror<List<String>> ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));

    public static List<String> getEntitiesSkip() {
        if(unload())
            return ImmutableList.of();
        return ENTITY_SKIP.getValue();
    }

    private static final PropertyMirror<List<String>> BLOCK_ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));

    public static List<String> getBlockEntitiesSkip() {
        if(unload())
            return ImmutableList.of();
        return BLOCK_ENTITY_SKIP.getValue();
    }

    private static ConfigContext CONTEXT;
    private static ConfigBranch BRANCH;
    private static boolean configLoaded = false;

    private static boolean unload() {
        return !configLoaded;
    }

    private static String getTranslatedItem(String s) {
        return new TranslatableComponent(s).getString();
    }

    public static void save() {
        if(CONTEXT != null) {
            try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(CONTEXT.path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
                FiberSerialization.serialize(CONTEXT.config, s, CONTEXT.serializer);
            } catch (IOException ignored) {
            }
        }
    }

    private static void init() {
        List<String> entityList = new ArrayList<>();
        entityList.add("create:stationary_contraption");

        List<String> blockList = new ArrayList<>();
        blockList.add("minecraft:beacon");

        BRANCH = ConfigTree.builder()
                .beginValue(getTranslatedItem("brute_force_rendering_culling.sampler"), ConfigTypes.DOUBLE.withValidRange(0.0, 1.0, 0.01), 0.05)
                .finishValue(SAMPLING::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.culling_map_update_delay"), ConfigTypes.INTEGER, 1)
                .finishValue(UPDATE_DELAY::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.cull_entity"), ConfigTypes.BOOLEAN, true)
                .finishValue(CULL_ENTITY::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.cull_chunk"), ConfigTypes.BOOLEAN, true)
                .finishValue(CULL_CHUNK::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.culling_entity_update_rate"), ConfigTypes.INTEGER, 20)
                .finishValue(CULLING_ENTITY_RATE::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.skip_culling_entities"), ConfigTypes.makeList(ConfigTypes.STRING), entityList)
                .finishValue(ENTITY_SKIP::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.skip_culling_block_entities"), ConfigTypes.makeList(ConfigTypes.STRING), blockList)
                .finishValue(BLOCK_ENTITY_SKIP::mirror)
                .build();
    }

    public static void loadConfig() {
        if(!configLoaded) {
            Config.init();
            try {
                Files.createDirectory(Paths.get("config"));
            } catch (IOException ignored) {
            }
            JanksonValueSerializer serializer = new JanksonValueSerializer(false);
            CONTEXT = new ConfigContext(BRANCH, Paths.get("config", CullingHandler.MOD_ID + ".json"), serializer);
            setupConfig(CONTEXT);
            configLoaded = true;
        }
    }

    private static void writeConfig(ConfigContext context) {
        try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(context.path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
            FiberSerialization.serialize(context.config, s, context.serializer);
        } catch (IOException ignored) {
        }
    }

    private static void setupConfig(ConfigContext context) {
        writeConfig(context);

        try (InputStream s = new BufferedInputStream(Files.newInputStream(context.path, StandardOpenOption.READ, StandardOpenOption.CREATE))) {
            FiberSerialization.deserialize(context.config, s, context.serializer);
        } catch (IOException | ValueDeserializationException ignored) {
        }
    }

    public record ConfigContext(ConfigTree config, Path path, JanksonValueSerializer serializer){}
}

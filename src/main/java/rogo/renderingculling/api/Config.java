package rogo.renderingculling.api;

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
    private static PropertyMirror<Double> SAMPLING = PropertyMirror.create(ConfigTypes.DOUBLE);
    public static double getSampling() {
        return SAMPLING.getValue();
    }
    private static PropertyMirror<Boolean> CULL_ENTITY = PropertyMirror.create(ConfigTypes.BOOLEAN);
    private static PropertyMirror<Boolean> CULL_CHUNK = PropertyMirror.create(ConfigTypes.BOOLEAN);
    private static PropertyMirror<Integer> UPDATE_DELAY = PropertyMirror.create(ConfigTypes.INTEGER);
    private static PropertyMirror<Integer> CULLING_ENTITY_RATE = PropertyMirror.create(ConfigTypes.INTEGER);

    private static PropertyMirror<List<String>> ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));
    private static PropertyMirror<List<String>> BLOCK_ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));

    private static ConfigContext CONTEXT;
    private static ConfigBranch BRANCH;
    private static String getTranslatedItem(String s) {
        String Translated = new TranslatableComponent(s).getString();
        return Translated;
    }

    public static void save() {
        if(CONTEXT != null) {
            writeConfig(CONTEXT);
        }
    }

    public static void init() {
        List<String> entityList = new ArrayList<>();
        entityList.add("create:stationary_contraption");

        List<String> blockList = new ArrayList<>();
        blockList.add("minecraft:beacon");

        BRANCH = ConfigTree.builder()
                .beginValue(getTranslatedItem("brute_force_rendering_culling.sampler"), ConfigTypes.DOUBLE.withValidRange(0.0, 1.0, 0.01), 0.2)
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
                .withComment("Example: \n" +
                        "[\"minecraft:creeper\", \"minecraft:zombie\"]")
                .finishValue(ENTITY_SKIP::mirror)

                .beginValue(getTranslatedItem("brute_force_rendering_culling.skip_culling_block_entities"), ConfigTypes.makeList(ConfigTypes.STRING), blockList)
                .withComment("Example: \n" +
                        "[\"minecraft:chest\", \"minecraft:mob_spawner\"]")
                .finishValue(BLOCK_ENTITY_SKIP::mirror)
                .build();
    }

    public static void loadConfig() {
        try {
            Files.createDirectory(Paths.get("config"));
        } catch (IOException ignored) {
        }
        JanksonValueSerializer serializer = new JanksonValueSerializer(false);
        CONTEXT = new ConfigContext(BRANCH, Paths.get("config", CullingHandler.MOD_ID + ".json"), serializer);
        setupConfig(CONTEXT);
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

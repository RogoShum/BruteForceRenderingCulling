package rogo.renderingculling.api;

import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static PropertyMirror<Double> SAMPLING = PropertyMirror.create(ConfigTypes.DOUBLE);
    public static PropertyMirror<Boolean> CULL_ENTITY = PropertyMirror.create(ConfigTypes.BOOLEAN);
    public static PropertyMirror<Boolean> CULL_CHUNK = PropertyMirror.create(ConfigTypes.BOOLEAN);
    public static PropertyMirror<Integer> UPDATE_DELAY = PropertyMirror.create(ConfigTypes.INTEGER);
    public static PropertyMirror<Integer> CULLING_ENTITY_RATE = PropertyMirror.create(ConfigTypes.INTEGER);

    public static PropertyMirror<List<String>> ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));
    public static PropertyMirror<List<String>> BLOCK_ENTITY_SKIP = PropertyMirror.create(ConfigTypes.makeList(ConfigTypes.STRING));

    public static void init() {
        List<String> entityList = new ArrayList<>();
        entityList.add("create:stationary_contraption");

        List<String> blockList = new ArrayList<>();
        blockList.add("minecraft:beacon");

        ConfigTree.builder()
                .withValue("Sampling multiple", ConfigTypes.DOUBLE.withValidRange(0.0, 1.0, 0.01), 0.2)
                .beginValue("multiple", ConfigTypes.DOUBLE, 0.2)
                .finishValue(SAMPLING::mirror)

                .withValue("Culling Map update delay", ConfigTypes.INTEGER.withValidRange(0, 10, 1), 1)
                .beginValue("delay frame", ConfigTypes.INTEGER, 1)
                .finishValue(UPDATE_DELAY::mirror)

                .withValue("Cull entity", ConfigTypes.BOOLEAN, true)
                .beginValue("enable cull entity", ConfigTypes.BOOLEAN, true)
                .finishValue(CULL_ENTITY::mirror)

                .withValue("Cull chunk", ConfigTypes.BOOLEAN, true)
                .beginValue("enable cull chunk", ConfigTypes.BOOLEAN, true)
                .finishValue(CULL_CHUNK::mirror)

                .withValue("Culling entity update frequency", ConfigTypes.INTEGER.withValidRange(0, 20, 1), 20)
                .beginValue("frequency", ConfigTypes.INTEGER, 20)
                .finishValue(CULLING_ENTITY_RATE::mirror)

                .withValue("Entity skip CULLING", ConfigTypes.makeList(ConfigTypes.STRING), entityList)
                .withComment("Entity that skip CULLING, example: \n" +
                        "[\"minecraft:creeper\", \"minecraft:zombie\"]")
                .beginValue("Entity ResourceLocation", ConfigTypes.makeList(ConfigTypes.STRING), entityList)
                .finishValue(ENTITY_SKIP::mirror)

                .withValue("Block Entity skip CULLING", ConfigTypes.makeList(ConfigTypes.STRING), blockList)
                .withComment("Block Entity that skip CULLING, example: \n" +
                        "[\"minecraft:chest\", \"minecraft:mob_spawner\"]")
                .beginValue("Block Entity ResourceLocation", ConfigTypes.makeList(ConfigTypes.STRING), blockList)
                .finishValue(BLOCK_ENTITY_SKIP::mirror)
                .build();
    }
}

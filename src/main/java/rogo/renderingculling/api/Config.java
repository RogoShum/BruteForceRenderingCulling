package rogo.renderingculling.api;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec.DoubleValue SAMPLING;
    public static ForgeConfigSpec.BooleanValue CULL_ENTITY;
    public static ForgeConfigSpec.BooleanValue CULL_CHUNK;
    public static ForgeConfigSpec.IntValue UPDATE_DELAY;
    public static ForgeConfigSpec.IntValue CULLING_ENTITY_RATE;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_SKIP;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_ENTITY_SKIP;

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT_BUILDER.push("Sampling multiple");
        SAMPLING = CLIENT_BUILDER.defineInRange("multiple", 0.03, 0.0, 1.0);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("Culling Map update delay");
        UPDATE_DELAY = CLIENT_BUILDER.defineInRange("delay frame", 1, 0, 10);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("Cull entity");
        CULL_ENTITY = CLIENT_BUILDER.define("enabled", true);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("Cull chunk");
        CULL_CHUNK = CLIENT_BUILDER.define("enabled", true);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("Culling entity update frequency");
        CULLING_ENTITY_RATE = CLIENT_BUILDER.defineInRange("frequency", 20, 0, 20);
        CLIENT_BUILDER.pop();

        List<String> list = new ArrayList<>();
        list.add("create:stationary_contraption");
        CLIENT_BUILDER.comment("Entity skip CULLING").push("Entity ResourceLocation");
        ENTITY_SKIP = CLIENT_BUILDER.comment("Entity that skip CULLING, example: \n" +
                "[\"minecraft:creeper\", \"minecraft:zombie\"]").defineList("list", list, (o -> o instanceof String));
        CLIENT_BUILDER.pop();

        list = new ArrayList<>();
        list.add("minecraft:beacon");
        CLIENT_BUILDER.comment("Block Entity skip CULLING").push("Block Entity ResourceLocation");
        BLOCK_ENTITY_SKIP = CLIENT_BUILDER.comment("Block Entity that skip CULLING, example: \n" +
                "[\"minecraft:chest\", \"minecraft:mob_spawner\"]").defineList("list", list, (o -> o instanceof String));
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }
}

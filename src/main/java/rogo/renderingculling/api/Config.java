package rogo.renderingculling.api;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static ForgeConfigSpec CLIENT_CONFIG;
    private static ForgeConfigSpec.DoubleValue SAMPLING;
    private static ForgeConfigSpec.BooleanValue CULL_ENTITY;
    private static ForgeConfigSpec.BooleanValue CULL_CHUNK;
    private static ForgeConfigSpec.IntValue UPDATE_DELAY;
    private static ForgeConfigSpec.IntValue CULLING_ENTITY_RATE;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_SKIP;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_ENTITY_SKIP;

    public static double getSampling() {
        if(unload())
            return 0.2;

        return SAMPLING.get();
    }

    public static void setSampling(double value) {
        SAMPLING.set(value);
        SAMPLING.save();
    }

    public static boolean getCullEntity() {
        if(unload() || !CullingHandler.gl33())
            return false;
        return CULL_ENTITY.get();
    }

    public static void setCullEntity(boolean value) {
        CULL_ENTITY.set(value);
        CULL_ENTITY.save();
    }

    public static boolean getCullChunk() {
        if(unload())
            return false;
        return CULL_CHUNK.get();
    }

    public static void setCullChunk(boolean value) {
        CULL_CHUNK.set(value);
        CULL_CHUNK.save();
    }

    public static int getDepthUpdateDelay() {
        if(unload())
            return 1;
        return UPDATE_DELAY.get();
    }

    public static void setDepthUpdateDelay(int value) {
        UPDATE_DELAY.set(value);
        UPDATE_DELAY.save();
    }

    public static int getCullingEntityRate() {
        if(unload())
            return 20;
        return CULLING_ENTITY_RATE.get();
    }

    public static void setCullingEntityRate(int value) {
        CULLING_ENTITY_RATE.set(value);
        CULLING_ENTITY_RATE.save();
    }

    public static List<? extends String> getEntitiesSkip() {
        if(unload())
            return ImmutableList.of();
        return ENTITY_SKIP.get();
    }

    public static List<? extends String> getBlockEntitiesSkip() {
        if(unload())
            return ImmutableList.of();
        return BLOCK_ENTITY_SKIP.get();
    }

    private static boolean unload() {
        return false;
    }

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT_BUILDER.push("Sampling multiple");
        SAMPLING = CLIENT_BUILDER.defineInRange("multiple", 0.05, 0.0, 1.0);
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

package rogo.renderingculling.api;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

public class ComponentUtil {
    public static MutableComponent translatable(String s) {
        return Component.translatable(s);
    }

    public static MutableComponent literal(String s) {
        return Component.literal(s);
    }
}

package rogo.renderingculling.api;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentUtil {
    public static TranslatableComponent translatable(String s) {
        return new TranslatableComponent(s);
    }

    public static TextComponent literal(String s) {
        return new TextComponent(s);
    }
}

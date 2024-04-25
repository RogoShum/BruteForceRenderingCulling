package rogo.renderingculling.util;

import me.cortex.nvidium.Nvidium;

public class NvidiumUtil {
    public static boolean nvidiumBfs() {
        return Nvidium.IS_ENABLED && Nvidium.config.async_bfs;
    }
}

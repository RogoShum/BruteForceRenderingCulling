package rogo.renderingculling.util;

import net.minecraft.core.BlockPos;
import org.lwjgl.system.CallbackI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class LifeTimer<T> {
    private final Map<T, Integer> usageTick;

    public LifeTimer() {
        usageTick = new HashMap<>();
    }

    public void tick(int clientTick, int count) {
        Iterator<Map.Entry<T, Integer>> iterator = usageTick.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<T, Integer> entry = iterator.next();
            int tick = entry.getValue();
            if (clientTick - tick > count) {
                iterator.remove();
            }
        }
    }

    public void updateUsageTick(T hash, int tick) {
        usageTick.put(hash, tick);
    }

    public boolean contains(T hash) {
        return usageTick.containsKey(hash);
    }

    public void clear() {
        usageTick.clear();
    }

    public int size() {
        return usageTick.size();
    }

    public void foreach(Consumer<T> consumer) {
        usageTick.keySet().forEach(consumer);
    }
}


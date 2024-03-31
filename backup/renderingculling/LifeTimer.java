package rogo.renderingculling;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    public void updateUsageTick(T blockPos, int tick) {
        usageTick.put(blockPos, tick);
    }

    public boolean contains(T pos) {
        return usageTick.containsKey(pos);
    }

    public void clear() {
        usageTick.clear();
    }
}


package rogo.renderingculling.api.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.ModLoader;
import rogo.renderingculling.util.LifeTimer;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import static net.minecraftforge.common.extensions.IForgeBlockEntity.INFINITE_EXTENT_AABB;

public class EntityCullingMap extends CullingMap {
    private final EntityMap entityMap = new EntityMap();

    public EntityCullingMap(int width, int height) {
        super(width, height);
    }

    @Override
    protected boolean shouldUpdate() {
        return true;
    }

    @Override
    int configDelayCount() {
        return Config.getDepthUpdateDelay();
    }

    @Override
    int bindFrameBufferId() {
        return CullingStateManager.ENTITY_CULLING_MAP_TARGET.frameBufferId;
    }

    public boolean isObjectVisible(Object o) {
        AABB aabb = ModLoader.getObjectAABB(o);

        if (aabb == INFINITE_EXTENT_AABB) {
            return true;
        }

        int idx = entityMap.getIndex(o);
        idx = 1 + idx * 4;
        if (entityMap.tempObjectTimer.contains(o))
            entityMap.addTemp(o, CullingStateManager.clientTickCount);

        if (idx > -1 && idx < cullingBuffer.limit()) {
            return (cullingBuffer.get(idx) & 0xFF) > 0;
        } else if(delayCount <= 0) {
            entityMap.addTemp(o, CullingStateManager.clientTickCount);
        }
        return true;
    }

    @Override
    public void readData() {
        super.readData();
        entityMap.clearNew();
    }

    public EntityMap getEntityTable() {
        return entityMap;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        entityMap.clear();
    }

    public static class EntityMap {
        private final HashMap<Object, Integer> indexMap = new HashMap<>();
        private final LifeTimer<Object> tempObjectTimer = new LifeTimer<>();
        private final HashSet<Object> newToTemp = new HashSet<>();

        public EntityMap() {
        }

        public void addObject(Object obj) {
            if (indexMap.containsKey(obj))
                return;
            if (obj instanceof Entity && ((Entity) obj).isAlive())
                indexMap.put(obj, indexMap.size());
            else if (obj instanceof BlockEntity && !((BlockEntity) obj).isRemoved())
                indexMap.put(obj, indexMap.size());
            else
                indexMap.put(obj, indexMap.size());
        }

        public void addTemp(Object obj, int tickCount) {
            tempObjectTimer.updateUsageTick(obj, tickCount);
            if(!tempObjectTimer.contains(obj)) {
                newToTemp.add(obj);
            }
        }

        public void copyTemp(EntityMap entityMap, int tickCount) {
            entityMap.tempObjectTimer.foreach(o -> addTemp(o, tickCount));
        }

        public Integer getIndex(Object obj) {
            if(newToTemp.contains(obj))
                return -1;
            return indexMap.getOrDefault(obj, -1);
        }

        public void clearNew() {
            newToTemp.clear();
        }

        public void clearIndexMap() {
            indexMap.clear();
        }

        public void tickTemp(int tickCount) {
            tempObjectTimer.tick(tickCount, 2);
        }

        public void addAllTemp() {
            tempObjectTimer.foreach(this::addObject);
        }

        public void clear() {
            indexMap.clear();
            tempObjectTimer.clear();
        }

        private void addAttribute(Consumer<Consumer<FloatBuffer>> consumer, AABB aabb, int index) {
            consumer.accept(buffer -> {
                buffer.put((float) index);

                float size = (float) Math.max(aabb.getXsize(), aabb.getZsize());
                buffer.put(size*1.5F+1F);
                buffer.put((float) aabb.getYsize()*1.5F+1F);

                Vec3 pos = aabb.getCenter();
                buffer.put((float) pos.x);
                buffer.put((float) pos.y);
                buffer.put((float) pos.z);
            });
        }

        public void addEntityAttribute(Consumer<Consumer<FloatBuffer>> consumer) {
            indexMap.forEach((o, index) -> {
                addAttribute(consumer, ModLoader.getObjectAABB(o), index);
            });
        }

        public int size() {
            return indexMap.size();
        }
    }

}

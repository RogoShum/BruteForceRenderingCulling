package rogo.renderingculling.api.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rogo.renderingculling.api.Config;
import rogo.renderingculling.api.CullingStateManager;
import rogo.renderingculling.api.ModLoader;
import rogo.renderingculling.util.IndexedSet;
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

        int idx = getEntityTable().getIndex(o);
        idx = 1 + idx * 4;
        if (getEntityTable().tempObjectTimer.contains(o))
            getEntityTable().addTemp(o, CullingStateManager.clientTickCount);

        if (idx > -1 && idx < cullingBuffer.limit()) {
            return (cullingBuffer.get(idx) & 0xFF) > 0;
        } else {
            getEntityTable().addTemp(o, CullingStateManager.clientTickCount);
        }
        return true;
    }

    @Override
    public void readData() {
        super.readData();
        getEntityTable().readUpload();
    }

    public EntityMap getEntityTable() {
        return entityMap;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        getEntityTable().clear();
    }

    public static class EntityMap {
        private final IndexedSet<Object> indexMap = new IndexedSet<>();
        private final LifeTimer<Object> tempObjectTimer = new LifeTimer<>();
        private HashSet<Object> uploadTemp = new HashSet<>();
        private HashSet<Object> readTemp = new HashSet<>();
        private HashMap<Object, Integer> uploadEntity = new HashMap<>();
        private HashMap<Object, Integer> readEntity = new HashMap<>();

        public EntityMap() {
        }

        public void addObject(Object obj) {
            if (obj instanceof Entity && ((Entity) obj).isAlive())
                indexMap.add(obj);
            else if (obj instanceof BlockEntity && !((BlockEntity) obj).isRemoved())
                indexMap.add(obj);
            else
                indexMap.add(obj);
        }

        public void addTemp(Object obj, int tickCount) {
            tempObjectTimer.updateUsageTick(obj, tickCount);
        }

        public void copyTemp(EntityMap entityMap, int tickCount) {
            entityMap.tempObjectTimer.foreach(o -> addTemp(o, tickCount));
            this.uploadTemp.addAll(entityMap.uploadTemp);
            this.uploadEntity.putAll(entityMap.uploadEntity);
            this.readTemp = uploadTemp;
            this.readEntity = uploadEntity;
        }

        public Integer getIndex(Object obj) {
            if(!readTemp.contains(obj))
                return -1;
            return readEntity.getOrDefault(obj, -1);
        }

        public void readUpload() {
            readTemp = uploadTemp;
            uploadTemp = new HashSet<>();
            readEntity = uploadEntity;
            uploadEntity = new HashMap<>();
        }

        public void clearUpload() {
            uploadTemp.clear();
            uploadEntity.clear();
        }

        public void clearIndexMap() {
            indexMap.clear();
        }

        public void tickTemp(int tickCount) {
            tempObjectTimer.tick(tickCount, 3);
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
                buffer.put(size+0.5F);
                buffer.put((float) aabb.getYsize()+0.5F);

                Vec3 pos = aabb.getCenter();
                buffer.put((float) pos.x);
                buffer.put((float) pos.y);
                buffer.put((float) pos.z);
            });
        }

        public void addEntityAttribute(Consumer<Consumer<FloatBuffer>> consumer) {
            clearUpload();
            indexMap.forEach((o, index) -> {
                addAttribute(consumer, ModLoader.getObjectAABB(o), index);
                uploadTemp.add(o);
                uploadEntity.put(o, index);
            });
        }

        public int size() {
            return indexMap.size();
        }
    }

}

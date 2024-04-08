package rogo.renderingculling.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rogo.renderingculling.util.LifeTimer;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.function.Consumer;

public class EntityCullingMap extends CullingMap {
    private final EntityMap entityMap = new EntityMap();

    public EntityCullingMap(int width, int height) {
        super(width, height);
    }

    @Override
    int delayCount() {
        return Config.UPDATE_DELAY.get();
    }

    @Override
    int bindFrameBufferId() {
        return CullingHandler.ENTITY_CULLING_MAP_TARGET.fbo;
    }

    public boolean isObjectVisible(Object o) {
        Box aabb = null;
        if(o instanceof BlockEntity) {
            aabb = ((BlockEntity) o).getRenderBoundingBox();
        } else if(o instanceof Entity) {
            aabb = ((Entity) o).getBoundingBox();
        } else if(o instanceof IAABBObject) {
            aabb = ((IAABBObject) o).getAABB();
        }

        if(aabb == INFINITE_EXTENT_AABB) {
            return true;
        }

        int idx = entityMap.getIndex(o);
        idx = 1+idx*4;
        if(entityMap.tempObjectTimer.contains(o))
            entityMap.addTemp(o, CullingHandler.INSTANCE.clientTickCount);

        if(idx > -1 && idx < cullingBuffer.limit()) {
            float cullingValue = (float) (cullingBuffer.get(idx) & 0xFF) / 255.0f;
            return cullingValue > 0.5;
        } else {
            entityMap.addTemp(o, CullingHandler.INSTANCE.clientTickCount);
        }
        return true;
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
        private int innerCount;

        public EntityMap() {}

        public void addObject(Object obj) {
            if(indexMap.containsKey(obj))
                return;
            if(obj instanceof Entity && ((Entity) obj).isAlive())
                indexMap.put(obj, indexMap.size());
            else if(obj instanceof BlockEntity && !((BlockEntity) obj).isRemoved())
                indexMap.put(obj, indexMap.size());
            else
                indexMap.put(obj, indexMap.size());
        }

        public void addTemp(Object obj, int tickCount) {
            tempObjectTimer.updateUsageTick(obj, tickCount);
        }

        public void copyTemp(EntityMap entityMap, int tickCount) {
            entityMap.tempObjectTimer.foreach(o-> addTemp(o, tickCount));
            innerCount = tickCount;
        }

        public Integer getIndex(Object obj) {
            return indexMap.getOrDefault(obj, -1);
        }

        public void tick(int tickCount) {
            indexMap.clear();
            if(innerCount < tickCount) {
                tempObjectTimer.tick(tickCount, 40);
                innerCount = tickCount;
            }
        }

        public void addAllTemp() {
            tempObjectTimer.foreach(this::addObject);
        }

        public void clear() {
            indexMap.clear();
            tempObjectTimer.clear();
            innerCount = 0;
        }

        private void addAttribute(Consumer<Consumer<FloatBuffer>> consumer, Box aabb, int index) {
            consumer.accept(buffer -> {
                buffer.put((float) index);

                float size = (float) Math.max(aabb.getXLength(), aabb.getZLength());
                buffer.put(size);
                buffer.put((float) aabb.getYLength());

                Vec3d pos = aabb.getCenter();
                buffer.put((float) pos.x);
                buffer.put((float) pos.y);
                buffer.put((float) pos.z);
            });
        }

        public void addEntityAttribute(Consumer<Consumer<FloatBuffer>> consumer) {
            indexMap.forEach((o, index) -> {
                if(o instanceof Entity) {
                    addAttribute(consumer, ((Entity) o).getBoundingBox(), index);
                } else if(o instanceof BlockEntity) {
                    addAttribute(consumer, ((BlockEntity) o).getRenderBoundingBox(), index);
                } else if (o instanceof IAABBObject) {
                    addAttribute(consumer, ((IAABBObject) o).getAABB(), index);
                }
            });
        }

        public int size() {
            return indexMap.size();
        }
    }

}

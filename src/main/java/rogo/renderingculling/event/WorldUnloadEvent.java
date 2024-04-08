package rogo.renderingculling.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;

public class WorldUnloadEvent {
    
    public static final Event<WorldUnloadEvent.Unload> WORLD_UNLOAD = EventFactory.createArrayBacked(WorldUnloadEvent.Unload.class, callbacks -> (clientWorld) -> {
        if (EventFactory.isProfilingEnabled()) {
            final Profiler profiler = clientWorld.getProfiler();
            profiler.push("bfrcClientWorldUnload");

            for (WorldUnloadEvent.Unload callback : callbacks) {
                profiler.push(EventFactory.getHandlerName(callback));
                callback.onWorldUnload(clientWorld);
                profiler.pop();
            }

            profiler.pop();
        } else {
            for (WorldUnloadEvent.Unload callback : callbacks) {
                callback.onWorldUnload(clientWorld);
            }
        }
    });


    @FunctionalInterface
    public interface Unload {
        void onWorldUnload(ClientWorld world);
    }
}

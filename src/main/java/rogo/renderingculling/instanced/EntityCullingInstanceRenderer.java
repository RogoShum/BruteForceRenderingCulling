package rogo.renderingculling.instanced;

import net.minecraft.client.render.VertexFormat;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

public class EntityCullingInstanceRenderer extends InstanceVertexRenderer {

    public EntityCullingInstanceRenderer() {
        super(VertexFormat.DrawMode.QUADS, new PixelVertexBuffer(0), EntityCullingInstanceRenderer::init, new EntityUpdateVertex(1));
    }

    private static void init(FloatBuffer buffer) {
        buffer.position(0);
        buffer.put(-1f);
        buffer.put(-1f);
        buffer.put(1f);
        buffer.put(-1f);
        buffer.put(1f);
        buffer.put(1f);
        buffer.put(-1f);
        buffer.put(1f);
        buffer.flip();
        GL15.glBufferData(34962, buffer, 35044);
    }
}

package rogo.renderingculling.instanced;

import rogo.renderingculling.instanced.attribute.GLFloatVertex;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

public class EntityUpdateVertex extends VertexAttrib {

    public EntityUpdateVertex(int index) {
        super (
                GLFloatVertex.createF1(index, "index"),
                GLFloatVertex.createF2(index+1, "Size"),
                GLFloatVertex.createF3(index+2, "EntityCenter")
        );
    }

    public void addAttrib(Consumer<FloatBuffer> bufferConsumer) {
        try {
            bufferConsumer.accept(this.buffer);
        } catch (Exception e) {
            this.buffer.position(0);
        }
    }

    @Override
    public void init(Consumer<FloatBuffer> bufferConsumer) {}

    @Override
    public boolean needUpdate() {
        return true;
    }
}

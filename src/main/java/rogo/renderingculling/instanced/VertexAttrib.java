package rogo.renderingculling.instanced;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
import rogo.renderingculling.instanced.attribute.GLVertex;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class VertexAttrib implements AutoCloseable {
    protected final List<GLVertex> vertices = new ArrayList<>();
    protected final int vertexID;
    protected final int vertexSize;
    private int vertexCount;
    protected FloatBuffer buffer = MemoryUtil.memAllocFloat(2097152);

    public VertexAttrib(GLVertex... vertices) {
        this.vertices.addAll(Arrays.stream(vertices).toList());
        vertexID = GlStateManager._glGenBuffers();
        int vertexSize = 0;
        if(this.vertices.size() > 1) {
            for(GLVertex vertex : vertices) {
                vertexSize += vertex.size()*vertex.elementType().getSize();
            }
        }
        this.vertexSize = vertexSize;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int count) {
        this.vertexCount = count;
    }

    public abstract void init(Consumer<FloatBuffer> bufferConsumer);

    public abstract void addAttrib(Consumer<FloatBuffer> bufferConsumer);

    public void bind() {
        GL15.glBindBuffer(34962, vertexID);

    }

    public void enableVertexAttribArray() {
        bind();
        if(needUpdate()) {
            buffer.flip();
            GL15.glBufferData(34962, buffer, 35048);
        }
        int offset = 0;

        for(GLVertex vertex : vertices) {
            GlStateManager._vertexAttribPointer(vertex.index(), vertex.size(), vertex.elementType().getGlType(), false, vertexSize, offset);
            GlStateManager._enableVertexAttribArray(vertex.index());
            if(needUpdate())
                GL33.glVertexAttribDivisor(vertex.index(), 1);
            offset+=vertex.size()*vertex.elementType().getSize();
        }
    }

    public void disableVertexAttribArray() {
        buffer.clear();
        for(GLVertex vertex : vertices) {
            GlStateManager._disableVertexAttribArray(vertex.index());
        }
    }

    public abstract boolean needUpdate();

    public void update(FloatBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    public void update(ByteBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    public void update(IntBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    public void update(DoubleBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    public void update(LongBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    public void update(ShortBuffer buffer) {
        GL15.glBindBuffer(34962, this.vertexID);
        GL15.glBufferData(34962, buffer, 35044);
    }

    @Override
    public void close() {
        RenderSystem.glDeleteBuffers(this.vertexID);
        MemoryUtil.memFree(this.buffer);
    }
}

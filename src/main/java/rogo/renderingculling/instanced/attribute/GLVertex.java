package rogo.renderingculling.instanced.attribute;

import net.minecraft.client.render.VertexFormatElement;

public abstract class GLVertex {
    private final int size;
    private final String name;
    private final int index;

    public GLVertex(int index, String name, int size) {
        this.size = size;
        this.name = name;
        this.index = index;
    }

    public String name() {
        return name;
    }

    public int index() {
        return index;
    }

    public int size() {
        return size;
    }

    public abstract VertexFormatElement.DataType elementType();
}

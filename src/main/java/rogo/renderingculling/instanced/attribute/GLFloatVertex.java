package rogo.renderingculling.instanced.attribute;


import net.minecraft.client.render.VertexFormatElement;

public class GLFloatVertex extends GLVertex{

    public GLFloatVertex(int index, String name, int size) {
        super(index, name, size);
    }

    public static GLFloatVertex createF1(int index, String name) {
        return new GLFloatVertex(index, name, 1);
    }

    public static GLFloatVertex createF2(int index, String name) {
        return new GLFloatVertex(index, name, 2);
    }

    public static GLFloatVertex createF3(int index, String name) {
        return new GLFloatVertex(index, name, 3);
    }

    public static GLFloatVertex createF4(int index, String name) {
        return new GLFloatVertex(index, name, 4);
    }

    @Override
    public VertexFormatElement.DataType elementType() {
        return VertexFormatElement.DataType.FLOAT;
    }
}

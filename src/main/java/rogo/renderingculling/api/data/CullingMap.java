package rogo.renderingculling.api.data;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import rogo.renderingculling.api.CullingStateManager;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_BGRA;

public abstract class CullingMap {
    protected final int pboId;
    protected final int width;
    protected final int height;
    protected final ByteBuffer cullingBuffer;
    protected boolean done;
    protected boolean transferred;
    protected int delayCount = 0;

    public CullingMap(int width, int height) {
        this.width = width;
        this.height = height;
        cullingBuffer = BufferUtils.createByteBuffer(width * height * 4);
        pboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, pboId);
        GL15.glBufferData(GL31.GL_PIXEL_PACK_BUFFER, (long) width * height * 4 * Float.BYTES, GL15.GL_DYNAMIC_READ);
        GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, 0);
        CullingStateManager.bindMainFrameTarget();
    }

    public boolean needTransferData() {
        return delayCount <= 0;
    }

    public void syncDelay(CullingMap map) {
        map.delayCount = this.delayCount;
    }

    public void transferData() {
        if (delayCount <= 0) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, bindFrameBufferId());
            GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, pboId);
            GL11.glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, 0);
            GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, 0);
            CullingStateManager.bindMainFrameTarget();
            delayCount = configDelayCount() + dynamicDelayCount();
        } else if (shouldUpdate()) {
            delayCount--;
        }
        if (delayCount <= 0) {
            setTransferred(true);
        }
    }

    protected abstract boolean shouldUpdate();

    public void readData() {
        GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, pboId);
        GL15.glGetBufferSubData(GL31.GL_PIXEL_PACK_BUFFER, 0, cullingBuffer);
        GL15.glBindBuffer(GL31.GL_PIXEL_PACK_BUFFER, 0);
        setTransferred(false);
    }

    abstract int configDelayCount();

    public int dynamicDelayCount() {
        if (CullingStateManager.fps > 100) {
            return CullingStateManager.fps / 100;
        }

        return 0;
    }

    abstract int bindFrameBufferId();

    public boolean isDone() {
        return done;
    }

    public void setDone() {
        done = true;
    }

    public void cleanup() {
        GL15.glDeleteBuffers(pboId);
        cullingBuffer.clear();
    }

    public boolean isTransferred() {
        return transferred;
    }

    public void setTransferred(boolean transferred) {
        this.transferred = transferred;
    }
}

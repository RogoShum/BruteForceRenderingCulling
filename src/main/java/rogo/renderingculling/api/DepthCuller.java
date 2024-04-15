package rogo.renderingculling.api;

public abstract class DepthCuller<T> {
    public abstract void updateVisibleChunks();

    public abstract T getResult();
}

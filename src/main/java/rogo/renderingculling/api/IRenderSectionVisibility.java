package rogo.renderingculling.api;

public interface IRenderSectionVisibility {
    boolean shouldCheckVisibility(int frame);
    void updateVisibleTick(int frame);

    int getPositionX();
    int getPositionY();
    int getPositionZ();
}

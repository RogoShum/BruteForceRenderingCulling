package rogo.renderingculling.api.impl;

public interface IRenderSectionVisibility {
    boolean shouldCheckVisibility(int frame);

    void updateVisibleTick(int frame);

    int getPositionX();

    int getPositionY();

    int getPositionZ();
}

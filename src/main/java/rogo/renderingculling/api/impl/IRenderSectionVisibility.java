package rogo.renderingculling.api.impl;

public interface IRenderSectionVisibility {
    boolean shouldCheckVisibility(int clientTick);

    void updateVisibleTick(int clientTick);

    int getPositionX();

    int getPositionY();

    int getPositionZ();
}

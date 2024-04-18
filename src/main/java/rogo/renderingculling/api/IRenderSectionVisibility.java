package rogo.renderingculling.api;

public interface IRenderSectionVisibility {
    boolean shouldCheckVisibility(int clientTick);

    void updateVisibleTick(int clientTick);

    int getPositionX();

    int getPositionY();

    int getPositionZ();

    boolean isSubmittedRebuild();

    void setSubmittedRebuild(boolean submited);

    boolean isSearched();

    void setSearch(boolean search);
}

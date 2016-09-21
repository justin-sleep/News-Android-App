package com.quarterfull.newsAndroid.events.podcast;

public class FeedPanelSlideEvent {

    public FeedPanelSlideEvent(boolean panelOpen) {
        this.panelOpen = panelOpen;
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }

    boolean panelOpen;
}

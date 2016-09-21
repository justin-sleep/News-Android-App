package com.quarterfull.newsAndroid.interfaces;

import com.quarterfull.newsAndroid.database.model.RssItem;

public interface IPlayPausePodcastClicked {
    void openPodcast(RssItem rssItem);
    void pausePodcast();
}

package com.quarterfull.newsAndroid.model;

import com.quarterfull.newsAndroid.database.model.Feed;

public class PodcastFeedItem {

    public PodcastFeedItem(Feed feed, int podcastCount) {
        this.mFeed = feed;
        this.mPodcastCount = podcastCount;
    }

    public Feed mFeed;
    public int mPodcastCount;

}

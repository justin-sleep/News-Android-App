package com.quarterfull.newsAndroid.events.podcast.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import com.quarterfull.newsAndroid.events.podcast.TogglePlayerStateEvent;

public class PodcastNotificationToggle extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new TogglePlayerStateEvent());
    }
}

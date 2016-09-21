package com.quarterfull.newsAndroid;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.quarterfull.newsAndroid.database.DatabaseConnectionOrm;
import com.quarterfull.newsAndroid.database.model.RssItem;
import com.quarterfull.newsAndroid.events.podcast.PodcastCompletedEvent;
import com.quarterfull.newsAndroid.events.podcast.RegisterVideoOutput;
import com.quarterfull.newsAndroid.events.podcast.UpdatePodcastStatusEvent;
import com.quarterfull.newsAndroid.helper.SizeAnimator;
import com.quarterfull.newsAndroid.helper.TeslaUnreadManager;
import com.quarterfull.newsAndroid.interfaces.IPlayPausePodcastClicked;
import com.quarterfull.newsAndroid.model.MediaItem;
import com.quarterfull.newsAndroid.model.PodcastItem;
import com.quarterfull.newsAndroid.model.TTSItem;
import com.quarterfull.newsAndroid.services.PodcastDownloadService;
import com.quarterfull.newsAndroid.services.PodcastPlaybackService;
import com.quarterfull.newsAndroid.view.PodcastSlidingUpPanelLayout;
import com.quarterfull.newsAndroid.view.ZoomableRelativeLayout;
import com.quarterfull.newsAndroid.widget.WidgetProvider;

public class PodcastFragmentActivity extends AppCompatActivity implements IPlayPausePodcastClicked {

    private static final String TAG = PodcastFragmentActivity.class.getCanonicalName();

    private PodcastPlaybackService mPodcastPlaybackService;
    private boolean mBound = false;
    private EventBus eventBus;
    private PodcastFragment mPodcastFragment;
    private int appHeight;
    private int appWidth;

    @Bind(R.id.videoPodcastSurfaceWrapper) ZoomableRelativeLayout rlVideoPodcastSurfaceWrapper;
    @Bind(R.id.sliding_layout) PodcastSlidingUpPanelLayout sliding_layout;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        eventBus = EventBus.getDefault();

        ButterKnife.bind(this);

        ViewTreeObserver vto = rlVideoPodcastSurfaceWrapper.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rlVideoPodcastSurfaceWrapper.readVideoPosition();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    rlVideoPodcastSurfaceWrapper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    rlVideoPodcastSurfaceWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        rlVideoPodcastSurfaceWrapper.setVisibility(View.INVISIBLE);

        UpdatePodcastView();

        if(isMyServiceRunning(PodcastPlaybackService.class, this)) {
            Intent intent = new Intent(this, PodcastPlaybackService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindPodcastService();
    }

    private void unbindPodcastService() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(hasWindowFocus) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation != lastOrientation) {
                sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                lastOrientation = currentOrientation;
            }
        }

        super.onWindowFocusChanged(hasWindowFocus);
    }



    int lastOrientation = -1;
    @Override
    protected void onResume() {
        eventBus.register(this);

        if(mPodcastPlaybackService != null && !mPodcastPlaybackService.isActive()) {
            sliding_layout.setPanelHeight(0);
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        eventBus.unregister(this);
        eventBus.post(new RegisterVideoOutput(null, null));

        TeslaUnreadManager.PublishUnreadCount(this);
        WidgetProvider.UpdateWidget(this);

        super.onPause();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PodcastPlaybackService.LocalBinder binder = (PodcastPlaybackService.LocalBinder) service;
            mPodcastPlaybackService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public PodcastItem getCurrentPlayingPodcast() {
        if(mPodcastPlaybackService != null)
            return mPodcastPlaybackService.getCurrentlyPlayingPodcast();
        return null;
    }

    public PodcastSlidingUpPanelLayout getSlidingLayout() {
        return sliding_layout;
    }

    public boolean handlePodcastBackPressed() {
        if(mPodcastFragment != null && sliding_layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            if (!mPodcastFragment.onBackPressed())
                sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    protected void UpdatePodcastView() {

        if(mPodcastFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mPodcastFragment).commitAllowingStateLoss();
        }

        mPodcastFragment = PodcastFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.podcast_frame, mPodcastFragment)
                .commitAllowingStateLoss();

        if(!currentlyPlaying)
            sliding_layout.setPanelHeight(0);
    }

    boolean currentlyPlaying = false;

    boolean surfaceInitialized = false;
    boolean isVideoViewVisible = true;
    @Subscribe
    public void onEvent(UpdatePodcastStatusEvent podcast) {
        //If file is loaded or preparing and podcast is paused/not running expand the view
        if((podcast.isFileLoaded() || podcast.isPreparingFile()) && !currentlyPlaying) {
            //Expand view
            sliding_layout.setPanelHeight((int) dipToPx(68));
            currentlyPlaying = true;
            Log.v(TAG, "expanding podcast view!");
        } else if(!(podcast.isPreparingFile() || podcast.isFileLoaded()) && currentlyPlaying) { //If file is not loaded or podcast is not preparing file and is playing
            //Hide view
            sliding_layout.setPanelHeight(0);
            currentlyPlaying = false;
            Log.v(TAG, "collapsing podcast view!");
        }

        if (podcast.isVideoFile()) {
            if((!isVideoViewVisible || !surfaceInitialized) && rlVideoPodcastSurfaceWrapper.isPositionReady()) {
                surfaceInitialized = true;
                isVideoViewVisible = true;

                rlVideoPodcastSurfaceWrapper.setVisibility(View.VISIBLE);
                //AlphaAnimator.AnimateVisibilityChange(rlVideoPodcastSurfaceWrapper, View.VISIBLE);


                SurfaceView surfaceView = new SurfaceView(this);
                surfaceView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                rlVideoPodcastSurfaceWrapper.addView(surfaceView);

                eventBus.post(new RegisterVideoOutput(surfaceView, rlVideoPodcastSurfaceWrapper));
                togglePodcastVideoViewAnimation();
            }
        } else if(isVideoViewVisible) {
            isVideoViewVisible = false;

            eventBus.post(new RegisterVideoOutput(null, null));

            rlVideoPodcastSurfaceWrapper.setVisibility(View.GONE);
            //AlphaAnimator.AnimateVisibilityChange(rlVideoPodcastSurfaceWrapper, View.GONE);

            rlVideoPodcastSurfaceWrapper.removeAllViews();
        }

    }

    @Subscribe
    public void onEvent(PodcastCompletedEvent podcastCompletedEvent) {
        sliding_layout.setPanelHeight(0);
        sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        currentlyPlaying = false;
    }

    /*
    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        //| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
    */

    private static final int animationTime = 300; //Milliseconds
    float oldScaleFactor = 1;
    boolean isFullScreen = false;
    float scaleFactor = 1;
    boolean useAnimation = false;

    @Subscribe
    public void onEvent() {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        if(isFullScreen) {
            rlVideoPodcastSurfaceWrapper.setDisableScale(false);
            togglePodcastVideoViewAnimation();

            //showSystemUI();
        } else {
            //hideSystemUI();

            rlVideoPodcastSurfaceWrapper.setDisableScale(true);
            oldScaleFactor = rlVideoPodcastSurfaceWrapper.getScaleFactor();

            final View view = rlVideoPodcastSurfaceWrapper;

            final float oldHeight = view.getLayoutParams().height;
            final float oldWidth = view.getLayoutParams().width;


            //view.setPivotX(oldWidth/2);
            //view.setPivotY(oldHeight/2);

            /*
            Display display = getWindowManager().getDefaultDisplay();
            float width = display.getWidth();  // deprecated
            float height = display.getHeight();  // deprecated
            */



            scaleFactor = appWidth / (float) view.getWidth();
            float newHeightTemp = oldHeight * scaleFactor;
            float newWidthTemp = oldWidth * scaleFactor;

            //view.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(100);
            //scaleView(view, 1f, scaleFactor, 1f, scaleFactor);


            if(newHeightTemp > appHeight) { //Could happen on Tablets or in Landscape Mode
                scaleFactor = appHeight / (float) view.getHeight();
                newHeightTemp = oldHeight * scaleFactor;
                newWidthTemp = oldWidth * scaleFactor;
            }


            final float newHeight = newHeightTemp;
            final float newWidth = newWidthTemp;
            float newXPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition() + (int) getResources().getDimension(R.dimen.activity_vertical_margin);// (appWidth / 2) + dipToPx(10);
            float newYPosition = (appHeight/2) + ((newHeight/2) - oldHeight);

            useAnimation = true;

            view.animate().x(newXPosition).y(newYPosition).setDuration(animationTime).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if(useAnimation) {
                        view.startAnimation(new SizeAnimator(view, newWidth, newHeight, oldWidth, oldHeight, animationTime).sizeAnimator);
                    }
                    useAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });


            //rlVideoPodcastSurfaceWrapper.animate().scaleX(scaleFactor).scaleY(scaleFactor).x(newXPosition).y(newYPosition).setDuration(500).setListener(onResizeListener);
            //surfaceView.animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(500);

            //oldScaleFactor
            //scaleFactor = dipToPx(oldWidth) / newWidth;
            //scaleFactor = (1/oldScaleFactor) * dipToPx(oldWidth) / newWidth;
            //scaleFactor = oldWidth / newWidth;

            scaleFactor = 1/scaleFactor;
        }

        isFullScreen = !isFullScreen;
    }


    public void togglePodcastVideoViewAnimation() {
        boolean isLeftSliderOpen = false;

        if(this instanceof NewsReaderListActivity && ((NewsReaderListActivity) this).drawerLayout != null) {
            isLeftSliderOpen = ((NewsReaderListActivity) this).drawerLayout.isDrawerOpen(GravityCompat.START);
        }

        int podcastMediaControlHeightDp = pxToDp((int) getResources().getDimension(R.dimen.podcast_media_control_height));

        if(sliding_layout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) { //On Tablets
            animateToPosition(podcastMediaControlHeightDp);
        } else if(isLeftSliderOpen) {
            animateToPosition(0);
        } else {
            animateToPosition(64);
        }
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public void animateToPosition(final int yPosition) {
        appHeight = getWindow().getDecorView().findViewById(android.R.id.content).getHeight();
        appWidth = getWindow().getDecorView().findViewById(android.R.id.content).getWidth();

        final View view = rlVideoPodcastSurfaceWrapper; //surfaceView

        if(scaleFactor != 1) {
            int oldHeight = view.getLayoutParams().height;
            int oldWidth = view.getLayoutParams().width;
            int newHeight = view.getLayoutParams().height *= scaleFactor;
            int newWidth = view.getLayoutParams().width *= scaleFactor;
            scaleFactor = 1;

            Animation animator = new SizeAnimator(view, newWidth, newHeight, oldWidth, oldHeight, animationTime).sizeAnimator;
            animator.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animateToPosition(yPosition);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animator);
        } else {
            int absoluteYPosition = appHeight - view.getHeight() - (int) getResources().getDimension(R.dimen.activity_vertical_margin) - (int) dipToPx(yPosition);
            float xPosition = rlVideoPodcastSurfaceWrapper.getVideoXPosition();

            //TODO podcast video is only working for newer android versions
            view.animate().x(xPosition).y(absoluteYPosition).setDuration(animationTime);
        }




        /*
        int height = (int)(view.getHeight() * scaleFactor);
        int width = (int)(view.getWidth() * scaleFactor);
        view.setScaleX(oldScaleFactor);
        view.setScaleY(oldScaleFactor);
        view.getLayoutParams().height = height;
        view.getLayoutParams().width = width;
*/
        oldScaleFactor = 1;


    }


    float dipToPx(float dip) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return px;
    }

    @VisibleForTesting
    public void openMediaItem(MediaItem mediaItem) {
        Intent intent = new Intent(this, PodcastPlaybackService.class);
        if(mediaItem instanceof TTSItem)
            intent.putExtra(PodcastPlaybackService.TTS_ITEM, mediaItem);
        else
            intent.putExtra(PodcastPlaybackService.PODCAST_ITEM, mediaItem);
        startService(intent);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void openPodcast(final RssItem rssItem) {
        final PodcastItem podcastItem = DatabaseConnectionOrm.ParsePodcastItemFromRssItem(this, rssItem);

        File file = new File(PodcastDownloadService.getUrlToPodcastFile(this, podcastItem.link, false));
        if(file.exists()) {
            podcastItem.link = file.getAbsolutePath();

            openMediaItem(podcastItem);
        } else if(!podcastItem.offlineCached) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                    .setNegativeButton("Abort", null)
                    .setNeutralButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PodcastDownloadService.startPodcastDownload(PodcastFragmentActivity.this, podcastItem);

                            Toast.makeText(PodcastFragmentActivity.this, "Starting download of podcast. Please wait..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setTitle("Podcast")
                    .setMessage("Choose if you want to download or stream the selected podcast");


            if(!podcastItem.mimeType.equals("youtube")) {
                alertDialog.setPositiveButton("Stream", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openMediaItem(podcastItem);
                    }
                });
            }

            alertDialog.show();
        }
    }

    @Override
    public void pausePodcast() {
        mPodcastPlaybackService.pause();
    }
}

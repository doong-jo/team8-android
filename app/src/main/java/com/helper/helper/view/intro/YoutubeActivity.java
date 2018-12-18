package com.helper.helper.view.intro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.helper.helper.R;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.view.login.LoginActivity;


public class YoutubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener{
    private final static String TAG = YoutubeActivity.class.getSimpleName() + "/DEV";

    private MyPlayerStateChangeListener m_playerStateListener;

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onAdStarted() { }

        @Override
        public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) { }

        @Override
        public void onLoaded(String arg0) { }

        @Override
        public void onLoading() { }

        @Override
        public void onVideoEnded() {
            SharedPreferencer.putBoolean(SharedPreferencer.IS_LAUNCHED, true);

            Intent intent = new Intent(YoutubeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        @Override
        public void onVideoStarted() { }

    }

    public YoutubeActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        SharedPreferences pref = SharedPreferencer.getSharedPreferencer(this, SharedPreferencer.IS_LAUNCH_STATE, Activity.MODE_PRIVATE);
        final boolean isLaunched = pref.getBoolean(SharedPreferencer.IS_LAUNCHED,false);
        final boolean isLogined = pref.getBoolean(SharedPreferencer.IS_LOGINED, false);

        if( isLaunched ) {
            if( isLogined ) {
                Intent intent = new Intent(YoutubeActivity.this, ScrollingActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(YoutubeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        } else {
            m_playerStateListener = new MyPlayerStateChangeListener();
            YouTubePlayerView playerView = findViewById(R.id.player);
            playerView.initialize(getString(R.string.google_api), this);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        youTubePlayer.setPlayerStateChangeListener(m_playerStateListener);

        if (!wasRestored) {
            youTubePlayer.loadVideo(getString(R.string.youtube_key));
            youTubePlayer.setFullscreen(true);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Intent intent = new Intent(YoutubeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}

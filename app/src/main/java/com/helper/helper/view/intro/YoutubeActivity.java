package com.helper.helper.view.intro;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.helper.helper.R;
import com.helper.helper.view.login.LoginActivity;


public class YoutubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener{
    private final static String TAG = YoutubeActivity.class.getSimpleName() + "/DEV";
    //https://www.youtube.com/watch?v=n6EydOVvJ00&feature=youtu.be
    private final String VIDEO_ID = "n6EydOVvJ00";
    private MyPlayerStateChangeListener m_playerStateListener;

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(
                com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {

        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
            Intent intent = new Intent(YoutubeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        @Override
        public void onVideoStarted() {
        }

    }

    public YoutubeActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        m_playerStateListener = new MyPlayerStateChangeListener();

        YouTubePlayerView playerView = findViewById(R.id.player);
        playerView.initialize(getString(R.string.google_api), this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        youTubePlayer.setPlayerStateChangeListener(m_playerStateListener);

        if (!wasRestored) {
            youTubePlayer.loadVideo(VIDEO_ID);
            youTubePlayer.setFullscreen(true);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Intent intent = new Intent(YoutubeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}

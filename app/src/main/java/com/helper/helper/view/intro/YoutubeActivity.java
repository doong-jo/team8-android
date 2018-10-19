package com.helper.helper.view.intro;

import android.Manifest;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.helper.helper.R;

public class YoutubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener{
    private final static String TAG = YoutubeActivity.class.getSimpleName() + "/DEV";
    //https://www.youtube.com/watch?v=n6EydOVvJ00&feature=youtu.be
    private final String VIDEO_ID = "n6EydOVvJ00";

    public YoutubeActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        YouTubePlayerView playerView = findViewById(R.id.player);
        playerView.initialize(getString(R.string.google_api), this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.cueVideo(VIDEO_ID);
            youTubePlayer.play();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
//        Fragment fragment = new JoinFragment();
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//
//        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
//            fragmentManager.popBackStack();
//        }
//
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        fragmentTransaction.add( R.id.fragmentPlace, fragment );
//        fragmentTransaction.addToBackStack(null);
//
//        fragmentTransaction.commit();
    }
}

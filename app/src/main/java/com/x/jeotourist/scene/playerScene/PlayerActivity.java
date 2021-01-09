package com.x.jeotourist.scene.playerScene;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.x.jeotourist.R;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class PlayerActivity extends AppCompatActivity {
    @Inject
    SimpleExoPlayer simpleExoPlayer;

    @Inject
    SimpleCache simpleCache;

    @Inject
    CacheDataSourceFactory cacheDataSourceFactory;

    @Inject
    DataSource.Factory dataSourceFactory;

    PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        playerView = findViewById(R.id.playerView);
        playVideo(getIntent().getStringExtra("url"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        simpleExoPlayer.setPlayWhenReady(false);
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        simpleExoPlayer.setPlayWhenReady(true);
    }


    private void playVideo(String url) {
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(
                new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(Uri.parse(url)));
        simpleExoPlayer.prepare(concatenatedSource);
        playerView.setPlayer(simpleExoPlayer);

    }
}
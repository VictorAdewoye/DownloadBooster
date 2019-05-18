package com.victoradewoye.downloadbooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private ProgressBar downloadProgressBar;

    private BroadcastReceiver progressBarDownloadStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        downloadProgressBar.setMax(4194308);

        this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        }

        FetchController.shared().setUpFetchController(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.registerForProgressBarUpdateDownloadingStatus();
    }

    public void clickDownload(View view) {
        FetchController.shared().parallelFetchFile(new IFetchFileDownload() {
            @Override
            public void onComplete(Object fileObject) {
//                Toast.makeText(MainActivity.this, " Download Successful,", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void errorOccured(String error) {
//                Toast.makeText(MainActivity.this, " Download Failed,", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerForProgressBarUpdateDownloadingStatus() {
        this.progressBarDownloadStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    final String action = intent.getAction();
                    if (action != null && action.equals("update")) {
                        if (intent.hasExtra("update_Value")) {

                            final long progress = intent.getLongExtra("update_Value", 0L);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (progress > downloadProgressBar.getProgress()) {
                                        downloadProgressBar.setProgress((int) progress);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("update");

        LocalBroadcastManager.getInstance(this).registerReceiver(this.progressBarDownloadStatus, intentFilter);
    }
}

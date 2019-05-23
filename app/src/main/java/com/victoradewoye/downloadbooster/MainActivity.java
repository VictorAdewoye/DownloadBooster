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
import android.text.Editable;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    private ProgressBar downloadProgressBar;

    private BroadcastReceiver progressBarDownloadStatus;

    @Bind(R.id.firstPickerName) TextView firstPickerName;
    @Bind(R.id.secondPickerName) TextView secondPickerName;
    @Bind(R.id.thirdPickerName) TextView thirdPickerName;
    @Bind(R.id.firstPickerValueProtocol) NumberPicker firstPickerValueProtocol;
    @Bind(R.id.secondPickerValueProtocol) NumberPicker secondPickerValueProtocol;
    @Bind(R.id.thirdPickerValueProtocol) TextView thirdPickerValueProtocol;

    @Bind(R.id.toggleSwitch) Switch toggleSwitch;

    @Bind(R.id.parallelDownload) Button parallelDownload;

    @Bind(R.id.serialDownload) Button serialDownload;
    @Bind(R.id.fileSize) TextView fileSize;
    @Bind(R.id.deleteButton) Button deleteButton;
    @Bind(R.id.sourceUrl) EditText sourceUrl;

    private String urlValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        }

        FetchController.shared().setUpFetchController(this);
        this.setUpSwitch();
        this.sourceUrl.setText("http://f39bf6aa4a.bwtest-aws.pravala.com/384MB.jar");
        FetchController.shared().setUpParameters(Integer.valueOf(this.thirdPickerValueProtocol.getText().toString()), this.secondPickerValueProtocol.getValue(), firstPickerValueProtocol.getValue());
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.registerForProgressBarUpdateDownloadingStatus();
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

    private void setUpSwitch() {
        serialDownload.setEnabled(false);
        parallelDownload.setEnabled(false);
        serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
        parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);

        this.toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    serialDownload.setEnabled(false);
                    serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
                    parallelDownload.setEnabled(true);
                    parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);


                } else {
                    serialDownload.setEnabled(true);
                    serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
                    parallelDownload.setEnabled(false);
                    parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
    }

    public void fullDownload(View view) {
        FetchController.shared().fetchFile(urlValue, new IFetchFileDownload() {
            @Override
            public void onComplete(Object fileObject) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " Download Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void errorOccured(String error) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " Download Failed,", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void parallelDownload(View view) {
        FetchController.shared().parallelFetchFile(urlValue, new IFetchFileDownload() {
            @Override
            public void onComplete(Object fileObject) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " Download Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void errorOccured(String error) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " Download Failed,", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @OnClick(R.id.deleteButton)
    public void onDeleteButtonClick(View v) {
        FetchController.shared().clearDirectory();
    }


    @OnTextChanged(value = R.id.sourceUrl, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable editable) {
        urlValue = this.sourceUrl.getText().toString().trim();

        if (!editable.toString().equals("")) {
            if (URLUtil.isValidUrl(urlValue)) {
                parallelDownload.setEnabled(false);
                serialDownload.setEnabled(true);
                serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
                parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            parallelDownload.setEnabled(false);
            serialDownload.setEnabled(false);
            serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
            parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
        }

    }
}

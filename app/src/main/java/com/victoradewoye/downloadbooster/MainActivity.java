package com.victoradewoye.downloadbooster;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.firstPickerName)
    TextView firstPickerName;
    @Bind(R.id.secondPickerName)
    TextView secondPickerName;
    @Bind(R.id.thirdPickerName)
    TextView thirdPickerName;
    @Bind(R.id.firstPickerValueProtocol)
    NumberPicker firstPickerValueProtocol;
    @Bind(R.id.secondPickerValueProtocol)
    NumberPicker secondPickerValueProtocol;
    @Bind(R.id.numOfChunksTextView)
    TextView numOfChunksTextView;
    @Bind(R.id.toggleSwitch)
    Switch toggleSwitch;
    @Bind(R.id.storageToggleSwitch)
    Switch storageToggleSwitch;
    @Bind(R.id.parallelDownload)
    Button parallelDownload;
    @Bind(R.id.serialDownload)
    Button serialDownload;
    @Bind(R.id.fileSize)
    TextView fileSize;
    @Bind(R.id.deleteButton)
    Button deleteButton;
    @Bind(R.id.sourceUrl)
    EditText sourceUrl;
    private ProgressBar downloadProgressBar;
    private BroadcastReceiver progressBarDownloadStatus;
    private String urlValue;
    private boolean isExternalStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        try {

            downloadProgressBar = findViewById(R.id.downloadProgressBar);

            this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                this.downloadProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            }

            FetchController.shared().setUpFetchController(this);

            this.setPicker();

            this.setUpSwitch();

            this.setUpStorageSwitch();

            this.sourceUrl.setText("http://f39bf6aa4a.bwtest-aws.pravala.com/384MB.jar");

            FetchController.shared().setUpParameters(Integer.valueOf(this.numOfChunksTextView.getText().toString()), this.secondPickerValueProtocol.getValue(), firstPickerValueProtocol.getValue(), isExternalStorage);
        } catch (Exception exception) {
            Toast.makeText(this, "An error occurred: " + exception.getMessage() + " please re-install the app", Toast.LENGTH_SHORT).show();
        }
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
                    if (action != null && action.equals("progressUpdate")) {

                        if (intent.hasExtra("progressUpdate_Value") && intent.hasExtra("progress_bar_max_value")) {
                            final long progress = intent.getLongExtra("progressUpdate_Value", 0L);

                            final long maxValue = intent.getLongExtra("progress_bar_max_value", 0L);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadProgressBar.setMax((int) maxValue);

                                    downloadProgressBar.setProgress((int) progress);
                                }
                            });

                            fileSize.setText("Downloaded file size: " + FetchController.shared().getTotalCacheFileSize());
                        } else {
                            downloadProgressBar.setProgress(0);

                            fileSize.setText("Downloaded file size: " + FetchController.shared().getTotalCacheFileSize());
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("progressUpdate");

        LocalBroadcastManager.getInstance(this).registerReceiver(this.progressBarDownloadStatus, intentFilter);
    }

    public void setPicker() {
        firstPickerValueProtocol.setSaveFromParentEnabled(false);
        firstPickerValueProtocol.setSaveEnabled(false);
        secondPickerValueProtocol.setSaveFromParentEnabled(false);
        secondPickerValueProtocol.setSaveEnabled(false);

        this.firstPickerValueProtocol.setDisplayedValues(null);
        this.secondPickerValueProtocol.setDisplayedValues(null);

        ArrayList<String> totalDownloadSizeValues = new ArrayList<>();

        for (int i = 1; i < 385; i++) {
            totalDownloadSizeValues.add(String.valueOf(i));
        }

        String[] newDisplayValues = new String[totalDownloadSizeValues.size() - 1];
        newDisplayValues = totalDownloadSizeValues.toArray(newDisplayValues);

        this.firstPickerValueProtocol.setMinValue(1);
        this.firstPickerValueProtocol.setMaxValue(384);
        this.firstPickerValueProtocol.setDisplayedValues(newDisplayValues);
        this.firstPickerValueProtocol.setWrapSelectorWheel(true);

        this.firstPickerValueProtocol.setValue(4);

        ArrayList<Integer> chunkSizeArray = FetchController.shared().factorsOf(firstPickerValueProtocol.getValue());

        String[] factorsArray = Utility.convertIntegerArrayListToStringArray(chunkSizeArray);

        secondPickerValueProtocol.setMinValue(1);
        secondPickerValueProtocol.setMaxValue(factorsArray.length);
        secondPickerValueProtocol.setDisplayedValues(factorsArray);
        this.secondPickerValueProtocol.setWrapSelectorWheel(false);

        this.numOfChunksTextView.setText(String.valueOf((firstPickerValueProtocol.getValue()) / secondPickerValueProtocol.getValue()));

        this.firstPickerValueProtocol.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                ArrayList<Integer> secondPickerChunkSizeArray = FetchController.shared().factorsOf(firstPickerValueProtocol.getValue());

                String[] factorsArray = Utility.convertIntegerArrayListToStringArray(secondPickerChunkSizeArray);

                secondPickerValueProtocol.setDisplayedValues(null);

                secondPickerValueProtocol.setMinValue(1);
                secondPickerValueProtocol.setMaxValue(factorsArray.length);
                secondPickerValueProtocol.setDisplayedValues(factorsArray);

                int chunkSize = secondPickerValueProtocol.getValue();

                if (chunkSize != 0) {
                    numOfChunksTextView.setText(String.valueOf(picker.getValue() / secondPickerChunkSizeArray.get(chunkSize - 1)));
                }

                FetchController.shared().setUpParameters(Integer.valueOf(numOfChunksTextView.getText().toString()), secondPickerChunkSizeArray.get(chunkSize - 1), firstPickerValueProtocol.getValue(), isExternalStorage);
            }
        });

        this.secondPickerValueProtocol.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                ArrayList<Integer> chunkArray = FetchController.shared().factorsOf(firstPickerValueProtocol.getValue());

                int dDownloadSize = firstPickerValueProtocol.getValue();
                int chunkSize = picker.getValue();

                if (chunkSize != 0) {
                    numOfChunksTextView.setText(String.valueOf(dDownloadSize / chunkArray.get(chunkSize - 1)));
                }

                FetchController.shared().setUpParameters(Integer.valueOf(numOfChunksTextView.getText().toString()), chunkArray.get(chunkSize - 1), firstPickerValueProtocol.getValue(), isExternalStorage);
            }

        });
    }

    private void setUpSwitch() {
        toggleSerialAndDownloadButton(false, false);

        this.toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleSerialAndDownloadButton(false, true);
                } else {
                    toggleSerialAndDownloadButton(true, false);
                }
            }
        });
    }

    private void setUpStorageSwitch() {
        isExternalStorage = false;

        this.storageToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        } else {
                            isExternalStorage = true;
                        }
                    } else {
                        isExternalStorage = true;
                    }
                } else {
                    isExternalStorage = false;
                }
            }
        });
    }

    //This method is called when the serial download button is clicked
    public void serialDownload(View view) {
        FetchController.shared().setUpParameters(Integer.valueOf(this.numOfChunksTextView.getText().toString()), this.secondPickerValueProtocol.getValue(), firstPickerValueProtocol.getValue(), isExternalStorage);

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
            public void errorOccurred(String error) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " The specified file has been downloaded or Download Failed, Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    //This method is called when the parallel download button is clicked
    public void parallelDownload(View view) {
        FetchController.shared().setUpParameters(Integer.valueOf(this.numOfChunksTextView.getText().toString()), this.secondPickerValueProtocol.getValue(), firstPickerValueProtocol.getValue(), isExternalStorage);

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
            public void errorOccurred(String error) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, " The specified file has been downloaded or Download Failed, Please try again later", Toast.LENGTH_SHORT).show();
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
                toggleSerialAndDownloadButton(true, false);
            } else {
                Toast.makeText(this, "Please enter a valid url", Toast.LENGTH_SHORT).show();
            }
        } else {
            toggleSerialAndDownloadButton(false, false);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isExternalStorage = true;
                }
        }
    }


    public void toggleSerialAndDownloadButton(boolean enableSerialDownloadButton, boolean enableParallelDownLoadButton) {
        if (enableSerialDownloadButton) {
            serialDownload.setEnabled(true);
            serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
        } else {
            serialDownload.setEnabled(false);
            serialDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
        }

        if (enableParallelDownLoadButton) {
            parallelDownload.setEnabled(true);
            parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
        } else {
            parallelDownload.setEnabled(false);
            parallelDownload.getBackground().setColorFilter(getResources().getColor(R.color.unselectedGrey), PorterDuff.Mode.SRC_ATOP);
        }

    }
}

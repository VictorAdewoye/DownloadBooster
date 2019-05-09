package com.victoradewoye.downloadbooster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FetchController.shared().setUpFetchController(this);
    }

    public void clickDownlaod(View view) {
        FetchController.shared().fetchFile(new IFetchFileDownload() {
            @Override
            public void onComplete(Object fileObject) {
                Toast.makeText(MainActivity.this, " Download Successful,", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void errorOccured(String error) {
                Toast.makeText(MainActivity.this, " Download Failed,", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

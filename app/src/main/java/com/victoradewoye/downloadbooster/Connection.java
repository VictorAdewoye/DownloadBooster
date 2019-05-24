package com.victoradewoye.downloadbooster;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {

    public Connection() {
        // Empty Constructor
    }

    public void downloadFileByRange(String url, IConnectionFileDownload callBack, long startChunkSize, long endChunkSize) {

        try {
            URL downloadUrl = new URL(url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) downloadUrl.openConnection();

            httpURLConnection.setRequestProperty("Range", "bytes=" + startChunkSize + "-" + endChunkSize);

            httpURLConnection.connect();

            int statusCode = httpURLConnection.getResponseCode();

            InputStream inputStream;

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK: // 200

                    inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccurred("Error Occurred when trying to connect with the Server");
                    }

                    break;

                case HttpURLConnection.HTTP_PARTIAL:
                    Log.i("called", "partial: ");

                    inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccurred("Error Occurred when trying to connect with the Server");
                    }

                    break;

                case HttpURLConnection.HTTP_FORBIDDEN:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    callBack.errorOccurred("No File Found");

                    break;
            }
        } catch (Exception exception) {
            callBack.errorOccurred(exception.getMessage());
        }

    }
}

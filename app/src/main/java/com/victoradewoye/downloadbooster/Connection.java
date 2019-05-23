package com.victoradewoye.downloadbooster;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {

    public Connection() {
        // Empty Constructor
    }

    public void downloadWholeFile(IConnectionFileDownload callBack) {

        try {
            URL url = new URL("http://f39bf6aa4a.bwtest-aws.pravala.com/384MB.jar");

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.connect();

            int statusCode = httpURLConnection.getResponseCode();

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK: // 200

                    InputStream inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccured("Error Occurred when trying to connect with the Server");
                    }

                    break;
                case HttpURLConnection.HTTP_FORBIDDEN:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    callBack.errorOccured("No File Found");

                    break;
            }
        } catch (Exception exception) {
            callBack.errorOccured(exception.getMessage());
        }
    }

    public void downloadFileByRange(String url, IConnectionFileDownload callBack, long startChunkSize, long endChunkSize) {

        String strLastModified = "";

        try {
            URL downloadUrl = new URL(url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) downloadUrl.openConnection();

            httpURLConnection.setRequestProperty("Range", "bytes=" + startChunkSize + "-" + endChunkSize);

            httpURLConnection.connect();

            int serverLengthFile = httpURLConnection.getContentLength();

            int statusCode = httpURLConnection.getResponseCode();

            InputStream inputStream;

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK: // 200

                    inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccured("Error Occurred when trying to connect with the Server");
                    }

                    break;

                case HttpURLConnection.HTTP_PARTIAL:
                    Log.i("called", "partial: ");

                    inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccured("Error Occurred when trying to connect with the Server");
                    }

                    break;

                case HttpURLConnection.HTTP_FORBIDDEN:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    callBack.errorOccured("No File Found");

                    break;
            }
        } catch (Exception exception) {
            callBack.errorOccured(exception.getMessage());
        }

    }
}

package com.victoradewoye.downloadbooster;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Connection {

    public Connection() {
        // Empty Constructor
    }

    public void downloadFileByRange(String url, boolean isParallelDownload, IConnectionFileDownload callBack, long startChunkSize, long endChunkSize) {
        HttpURLConnection httpURLConnection = null;

        try {
            URL downloadUrl = new URL(url);

            httpURLConnection = (HttpURLConnection) downloadUrl.openConnection();

            httpURLConnection.setRequestMethod("GET");

            httpURLConnection.setRequestProperty("Range", "bytes=" + startChunkSize + "-" + endChunkSize);

            httpURLConnection.connect();

            int statusCode = httpURLConnection.getResponseCode();

            InputStream inputStream = null;

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
                    inputStream = httpURLConnection.getInputStream();

                    if (inputStream != null) {
                        callBack.getFileResult(inputStream);
                    } else {
                        callBack.errorOccurred("Error Occurred when trying to connect with the Server");
                    }

                    break;

                case HttpURLConnection.HTTP_FORBIDDEN:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    callBack.errorOccurred("No Server File Found");

                    break;
            }
        } catch (MalformedURLException exception) {
            callBack.errorOccurred("Ensure that the url input is correct:" + exception.getMessage());
        } catch (IOException exception) {
            callBack.errorOccurred(exception.getMessage());
        } finally {
            if (httpURLConnection != null) {
                /*// The normal practice is to disconnect the connection after use, upon researching some school of thought thinks it is too expensive to open and close socket everytime.
                    - I was able to achieve the disconnect for the serial download case but not for the parallel download which involves multiple threads.
                 - The connection has to be open while this operation is ongoing.
                 - https://techblog.bozho.net/caveats-of-httpurlconnection/*/

                if (!isParallelDownload) {
                    httpURLConnection.disconnect();
                }
            }
        }
    }
}

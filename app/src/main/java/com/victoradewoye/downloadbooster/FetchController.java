package com.victoradewoye.downloadbooster;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class FetchController {
    private Connection connection;

    private Context context;

    private static FetchController instance = null;

    private File fileStorageDirectory;

    public FetchController() {

    }

    protected static FetchController shared() {
        if (instance == null) {
            instance = new FetchController();
        }

        return instance;
    }

    protected void setUpFetchController(Context context) {
        this.context = context;

        this.connection = new Connection();

        this.fileStorageDirectory = new File(context.getFilesDir() + "/CarnegieDownload/");

        this.checkDirectory(this.fileStorageDirectory);
    }

    private void checkDirectory(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()); {
                //TODO: Handle Error
            }
        }

    }

    public void fetchFile (final IFetchFileDownload callBack) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int maxNumberOfServerRequest = 4;

                final long totalDownloadSize = 4194308;

                long endChunkSize = 1048577; // this the equivalent of 1MB in bytes.

                long startChunkSize = 0;

                String path = fileStorageDirectory.getPath();

                final File outputFile = new File(path + File.separator + "384Test.jar");

                for (int i = 0; i < maxNumberOfServerRequest; i++) {

                    long outputFileSize = outputFile.length();

                    if (outputFileSize != 0) {
                        startChunkSize = outputFile.length();
                    }

                    if (!(outputFileSize >= totalDownloadSize)) {
                        connection.downloadFileByRange(new IConnectionFileDownload() {
                            @Override
                            public void getFileResult(InputStream inputStream) {

                                try {
                                    OutputStream outputStream = outputFile.length() == 0 ? new FileOutputStream(outputFile) :  new FileOutputStream(outputFile, true);

                                    byte[] buffer = new byte[16 * 1500];

                                    int bytesRead = 0;

                                    while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                    outputStream.close();

                                    if(outputFile.length() >= totalDownloadSize ) {
                                        callBack.onComplete(outputFile);
                                    }
                                } catch(Exception exception) {
                                    callBack.errorOccured(exception.getMessage());
                                }
                            }

                            @Override
                            public void errorOccured(String error) {
                                Log.i("Report", "errorOccured: ");
                            }
                        }, startChunkSize, endChunkSize);

                        endChunkSize = endChunkSize + 1048577;
                    } else {
                        Log.i("iNFO", "THE SPECIFIED FILE RANGE HAS BEEN DOWNLOADED: ");

                        break;
                    }

                }
            }
        });
    }
}

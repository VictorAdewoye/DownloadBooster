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
                connection.downloadWholeFile(new IConnectionFileDownload() {
                    @Override
                    public void getFileResult(InputStream inputStream) {
                        String path = fileStorageDirectory.getPath();

                        File outputFile = new File(path + File.separator + "348Test.jar");

                        try {
                            OutputStream outputStream = new FileOutputStream(outputFile);

                            byte[] buffer = new byte[16 * 1500];

                            int bytesRead = 0;

                            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            outputStream.close();

                            callBack.onComplete(outputFile);

                        } catch(Exception exception) {
                            callBack.errorOccured(exception.getMessage());
                        }
                    }

                    @Override
                    public void errorOccured(String error) {
                        Log.i("Report", "errorOccured: ");
                    }
                });
            }
        });
    }
}

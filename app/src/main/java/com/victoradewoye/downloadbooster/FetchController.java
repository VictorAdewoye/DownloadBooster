package com.victoradewoye.downloadbooster;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class FetchController {
    private static FetchController instance = null;
    private Connection connection;
    private Context context;
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
            if (!file.mkdirs()) ;
            {
                //TODO: Handle Error
            }
        }

    }

    public void fetchFile(final IFetchFileDownload callBack) {
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

                    final long outputFileSize = outputFile.length();

                    if (outputFileSize != 0) {
                        startChunkSize = outputFile.length();
                    }

                    if (!(outputFileSize >= totalDownloadSize)) {
                        connection.downloadFileByRange(new IConnectionFileDownload() {
                            @Override
                            public void getFileResult(InputStream inputStream) {

                                try {
                                    OutputStream outputStream = outputFile.length() == 0 ? new FileOutputStream(outputFile) : new FileOutputStream(outputFile, true);

                                    byte[] buffer = new byte[16 * 1500];

                                    int bytesRead = 0;

                                    long total = outputFileSize;

                                    while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                        outputStream.write(buffer, 0, bytesRead);

                                        total += bytesRead;

                                        Intent intent = new Intent("update");

                                        intent.putExtra("update_Value", total);

                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    }
                                    outputStream.close();

                                    if (outputFile.length() >= totalDownloadSize) {
                                        callBack.onComplete(outputFile);
                                    }

                                } catch (Exception exception) {
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

    public void parallelFetchFile(final IFetchFileDownload callBack) {

        int MYTHREADS = 4;

        final ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);

        final List<Future<InputStream>> futures = new ArrayList<>();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final long totalDownloadSize = 4194308;

                long endChunkSize = 1048577; // this the equivalent of 1MB in bytes.

                long startChunkSize = 0;

                String path = fileStorageDirectory.getPath();

                final File outputFile = new File(path + File.separator + "384Test.jar");

                for (long i = startChunkSize; i < totalDownloadSize; i += 1048577) {

                    Future<InputStream> future = executor.submit(parallelRunnableObject(i, endChunkSize));

                    futures.add(future);

                    endChunkSize += 1048577;

                }

                for (Future<InputStream> f : futures) {
                    final long outputFileSize = outputFile.length();
                    try {
                        OutputStream outputStream = outputFile.length() == 0 ? new FileOutputStream(outputFile) : new FileOutputStream(outputFile, true);

                        byte[] buffer = new byte[16 * 1500];

                        int bytesRead = 0;

                        long total = outputFileSize;

                        InputStream inputStream = f.get();

                        if (inputStream != null) {

                            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                outputStream.write(buffer, 0, bytesRead);

                                total += bytesRead;

                                Intent intent = new Intent("update");

                                intent.putExtra("update_Value", total);

                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }

                        }
                        outputStream.close();

                        if (outputFile.length() >= totalDownloadSize) {
                            callBack.onComplete(outputFile);
                        }

                    } catch (InterruptedException | ExecutionException ex) {
                        callBack.errorOccured(ex.getMessage());
                    } catch (Exception exception) {
                        callBack.errorOccured(exception.getMessage());
                    }

                }

                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                executor.shutdown();
            }
        });
    }


    private Callable<InputStream> parallelRunnableObject(final long startChunkSize, final long endChunkSize) {

        Callable<InputStream> chunkRequest = new Callable<InputStream>() {
            @Override
            public InputStream call() throws Exception {
                final InputStream[] chunkInputStream = {null};

                connection.downloadFileByRange(new IConnectionFileDownload() {
                    @Override
                    public void getFileResult(InputStream inputStream) {

                        chunkInputStream[0] = inputStream;
                    }

                    @Override
                    public void errorOccured(String error) {
                        Log.i("Report", "errorOccured: ");
                    }
                }, startChunkSize, endChunkSize);

                return chunkInputStream[0];
            }
        };

        return chunkRequest;
    }

}

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    public void fetchFile(final String url, final IFetchFileDownload callBack) {
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
                        connection.downloadFileByRange(url, new IConnectionFileDownload() {
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
    public void parallelFetchFile(final String url, final IFetchFileDownload callBack) {
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

    public ArrayList<Integer> factorsOf(int num) {
        ArrayList<Integer> factors = new ArrayList<>();

            for (int i = 1; i <= Math.sqrt(num); i++) {
                if (num % i == 0) {
                    int f1 = i;
                    int f2 = num / i;

                    if (f1 <= f2) {
                        if (!factors.contains(f1)) {
                            factors.add(f1);
                            if (!factors.contains(f2)) {
                                factors.add(f2);
                            }
                        }
                    }
                }
            }

        Collections.sort(factors);

        return factors;
    }


    private long getCacheFolderSize(File file) {
        long size = 0;

        if (file.isDirectory()) {
            if (file.listFiles() != null) {
                for (File subFileChild : file.listFiles()) {
                    if (subFileChild.isFile()) {
                        size += subFileChild.length();
                    } else {
                        size += getCacheFolderSize(subFileChild);
                    }
                }
            } else {
                return 0L;
            }
        } else {
            return file.length();
        }

        return size;
    }

    private String readableFolderSize(long size) {
        if (size <= 0) {
            return "Zero KB";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    protected String getTotalCacheFileSize() {
        long totalSize = getCacheFolderSize(this.fileStorageDirectory);

        return readableFolderSize(totalSize);
    }

    // Delete Files of a specific Directory Folder.
    protected void clearDirectory() {
        File directoryFile = this.fileStorageDirectory;

        if (directoryFile.isDirectory()) {
            File fileToBeDeleted = null;
            String[] children = directoryFile.list();

            for (int i = 0; i < children.length; i++) {
                fileToBeDeleted = new File(directoryFile, children[i]);

                fileToBeDeleted.delete();
            }
        } else if (directoryFile.isFile()) {
            directoryFile.delete();
        } else {
            Log.i("Error", "emptyDirectory: Did not work");
        }

        if (getCacheFolderSize(this.fileStorageDirectory) <= 0) {
            Intent intent = new Intent("update");

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

}

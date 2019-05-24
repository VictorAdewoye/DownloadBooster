package com.victoradewoye.downloadbooster;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
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
    private File externalFileStorageDirectory;

    private int numberOfChunks;
    private long chunkSize;
    private long downloadSize;

    private boolean isExternalStorage;


    private Thread myThread = null;

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

        this.externalFileStorageDirectory =  new File (Environment.getExternalStorageDirectory() + "/CarnegieDownload/");

        this.checkDirectory(this.fileStorageDirectory);

        this.checkDirectory(this.externalFileStorageDirectory);
    }

    private void checkDirectory(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) ;
            {
                //TODO: Handle Error
            }
        }

    }

    public void setUpParameters(int numberOfChunks, long chunkSize, long totalDownloadSize, boolean isExternalStorage) {

        this.numberOfChunks = numberOfChunks;

        this.chunkSize = chunkSize * 1048577;

        this.downloadSize = totalDownloadSize * 1048577;

        this.isExternalStorage = isExternalStorage;
    }

    public void fetchFile(final String url, final IFetchFileDownload callBack) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int maxNumberOfServerRequest = numberOfChunks;

                final long totalDownloadSize = downloadSize;

                long endChunkSize = chunkSize; // this the equivalent of 1MB in bytes.

                long startChunkSize = 0;

                String path = fileStorageDirectory.getPath();

                final File outputFile = isExternalStorage ? new File(externalFileStorageDirectory.getPath() + File.separator + "384Test.jar") : new File(path + File.separator + "384Test.jar");

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

                                    long sizeofFileDownloaded = outputFileSize;

                                    while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                        outputStream.write(buffer, 0, bytesRead);

                                        sizeofFileDownloaded += bytesRead;

                                        Intent intent = new Intent("update");

                                        intent.putExtra("update_Value", sizeofFileDownloaded);

                                        intent.putExtra("progress_bar_max_value", totalDownloadSize);

                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    }
                                    outputStream.flush();

                                    outputStream.close();

                                    if (outputFile.length() >= totalDownloadSize) {
                                        callBack.onComplete(outputFile);
                                    }

                                } catch (Exception exception) {
                                    callBack.errorOccurred(exception.getMessage());
                                }
                            }

                            @Override
                            public void errorOccurred(String error) {
                                Log.i("Report", "errorOccured: ");
                            }
                        }, startChunkSize, endChunkSize);

                        endChunkSize += chunkSize;
                    } else {
                        callBack.errorOccurred("The specified file size has been downloaded");

                        break;
                    }
                }
            }
        });
    }

    public void parallelFetchFile(final String url, final IFetchFileDownload callBack) {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int parallelThreads = numberOfChunks;

                final ExecutorService executor = Executors.newFixedThreadPool(parallelThreads);

                final List<Future<InputStream>> futures = new ArrayList<>();

                final long totalDownloadSize = downloadSize;

                long endChunkSize = chunkSize; // this the equivalent of 1MB in bytes. and it is also configurable

                long startChunkSize = 0;

                String path = fileStorageDirectory.getPath();

                final File outputFile = isExternalStorage ? new File(externalFileStorageDirectory.getPath() + File.separator + "384Test.jar") : new File(path + File.separator + "384Test.jar");

                if (!(outputFile.length() >= totalDownloadSize)) {
                    for (long i = startChunkSize; i < totalDownloadSize; i += chunkSize) {

                        Future<InputStream> future = executor.submit(parallelRunnableObject(url, i, endChunkSize, callBack));

                        futures.add(future);

                        endChunkSize += chunkSize;

                    }

                    for (Future<InputStream> f : futures) {
                        final long outputFileSize = outputFile.length();
                        try {
                            OutputStream outputStream = outputFile.length() == 0 ? new FileOutputStream(outputFile) : new FileOutputStream(outputFile, true);

                            byte[] buffer = new byte[16 * 1500];

                            int bytesRead = 0;

                            long sizeofFileDownloaded = outputFileSize;

                            InputStream inputStream = f.get();

                            if (inputStream != null) {

                                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                                    outputStream.write(buffer, 0, bytesRead);

                                    sizeofFileDownloaded += bytesRead;

                                    Intent intent = new Intent("update");

                                    intent.putExtra("update_Value", sizeofFileDownloaded);

                                    intent.putExtra("progress_bar_max_value", totalDownloadSize);

                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                }

                            }
                            outputStream.flush();

                            outputStream.close();

                            if (outputFile.length() >= totalDownloadSize) {
                                callBack.onComplete(outputFile);
                            }

                        } catch (InterruptedException | ExecutionException ex) {
                            callBack.errorOccurred(ex.getMessage());
                        } catch (Exception exception) {
                            callBack.errorOccurred(exception.getMessage());
                        }
                    }

                    try {
                        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    executor.shutdown();

//                    myThread.interrupt();
                } else {
                    callBack.errorOccurred("The specified file size has been downloaded");
                }
            }
        });

        myThread.start();

    }

    public ArrayList<Integer> factorsOf(int num) { // This method returns the factors of download size and uses it as different chunk sizes the user can pick from
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


    // Private Methods
    private Callable<InputStream> parallelRunnableObject(final String url, final long startChunkSize, final long endChunkSize, final IFetchFileDownload callBack) {

        Callable<InputStream> chunkRequest = new Callable<InputStream>() {
            @Override
            public InputStream call() throws Exception {
                final InputStream[] chunkInputStream = {null};

                connection.downloadFileByRange(url, new IConnectionFileDownload() {
                    @Override
                    public void getFileResult(InputStream inputStream) {

                        chunkInputStream[0] = inputStream;
                    }

                    @Override
                    public void errorOccurred(String error) {
                        callBack.errorOccurred("Error occurred while trying to download the file, Please try again later");
                    }
                }, startChunkSize, endChunkSize);

                return chunkInputStream[0];
            }
        };

        return chunkRequest;
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
        long totalSize = isExternalStorage ? getCacheFolderSize(this.externalFileStorageDirectory):getCacheFolderSize(this.fileStorageDirectory);

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

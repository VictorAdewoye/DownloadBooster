package com.victoradewoye.downloadbooster;

import java.io.InputStream;

public interface IConnectionFileDownload {
    void getFileResult(InputStream inputStream);
    void errorOccured(String error);
}

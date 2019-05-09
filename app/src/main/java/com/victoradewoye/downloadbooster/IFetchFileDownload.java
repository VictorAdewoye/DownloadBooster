package com.victoradewoye.downloadbooster;

import java.io.InputStream;

public interface IFetchFileDownload {
    void onComplete(Object fileObject);
    void errorOccured(String error);
}

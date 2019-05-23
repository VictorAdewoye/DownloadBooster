package com.victoradewoye.downloadbooster;

public interface IFetchFileDownload {
    void onComplete(Object fileObject);

    void errorOccured(String error);
}

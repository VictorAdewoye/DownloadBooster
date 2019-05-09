package com.victoradewoye.downloadbooster;

public class FetchController {
    
    private static FetchController instance = null;


    public FetchController() {

    }

    protected static FetchController shared() {
        if (instance == null) {
            instance = new FetchController();
        }

        return instance;
    }
}

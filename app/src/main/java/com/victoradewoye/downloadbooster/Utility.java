package com.victoradewoye.downloadbooster;

import java.util.ArrayList;

public class Utility {

    public static String[] convertIntegerArrayListToStringArray(ArrayList<Integer> integerArrayList) {
        ArrayList<String> stringFactorsArray = new ArrayList<>();

        for (Integer i: integerArrayList) {
            stringFactorsArray.add(String.valueOf(i));
        }

        return stringFactorsArray.toArray(new String[]{});
    }
}

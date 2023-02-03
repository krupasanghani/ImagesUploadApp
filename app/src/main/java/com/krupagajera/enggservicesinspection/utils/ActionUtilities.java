package com.krupagajera.enggservicesinspection.utils;


import android.content.Context;
import android.widget.Toast;

public class ActionUtilities {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
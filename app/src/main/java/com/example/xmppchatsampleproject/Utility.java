package com.example.xmppchatsampleproject;

import android.content.Context;
import android.net.ConnectivityManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utility {


    /***
     *
     * @param milliseconds time in epoc format
     * @return plain date string
     */
    public static String getFormattedDate(long milliseconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }


    /**
     * return if network is availabe or not
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (NullPointerException n) {
            return false;
        }
    }

}

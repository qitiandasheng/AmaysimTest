package com.example.amaysimtest;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.ContentValues.TAG;

/**
 * this class contains a static method for sending request to the server
 */

public class HttpUtil {
    //the URLs for JSON server data query
    public static final String ACCOUNTS_URL="http://10.0.2.2:3000/data";
    public static final String PRODUCTS_URL="http://10.0.2.2:3000/included/4000";
    public static final String SERVICES_URL="http://10.0.2.2:3000/included/0468874507";
    public static final String SUBSCRIPTIONS_URL="http://10.0.2.2:3000/included/0468874507-0";

    /**
     * send request by okhttp, and this is an static method, so that this intance will manage all
     * server connection tasks.
     *
     * @param address
     * @param callback
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "url: "+address);
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}

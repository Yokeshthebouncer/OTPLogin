package com.wassup.laundry1;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.facebook.accountkit.AccountKit;

/**
 * Created by Administrator on 7/28/2016.
 */
public class AppController extends Application {
    public static final String TAG = AppController.class
            .getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();

        AccountKit.initialize(getApplicationContext());
    }
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}

package com.guolina.httpdemo;

import android.app.Application;
import android.os.FileUriExposedException;

import com.guolina.httpdemo.utils.FileUtils;

/**
 * Created by guolina on 2017/5/31.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileUtils.init(getApplicationContext());
    }
}

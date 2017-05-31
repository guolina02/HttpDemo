package com.guolina.httpdemo.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by guolina on 2017/5/31.
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static String getOkHttpDownloadPath() {
        String downloadPath;
        if (hasExternalStorage()) {
            downloadPath = getExternalStoragePath();
        } else {
            downloadPath = getInternalStoragePath();
        }
        downloadPath += "/download";
        Log.d(TAG, "gln_DownloadPath: " + downloadPath);
        File file = new File(downloadPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return downloadPath;
    }

    private static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private static String getExternalStoragePath() {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/HttpDemo/file";
        Log.d(TAG, "gln_externalStorage: " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
        return filePath;
    }

    private static String getInternalStoragePath() {
        return mContext.getFilesDir().getPath();
    }
}

package com.guolina.httpdemo.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

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

    public static File getOkHttpUploadFile() {
        String uploadPath;
        if (hasExternalStorage()) {
            uploadPath = getExternalStoragePath();
        } else {
            uploadPath = getInternalStoragePath();
        }
        uploadPath += "/upload";
        Log.d(TAG, "gln_UploadPath: " + uploadPath);
        File pathFile = new File(uploadPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        File file = new File(uploadPath + "/test.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static boolean hasExternalStorage() {
//        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        return false;
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

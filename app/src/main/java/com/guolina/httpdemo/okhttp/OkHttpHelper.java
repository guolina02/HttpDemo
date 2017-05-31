package com.guolina.httpdemo.okhttp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by guolina on 2017/5/25.
 */
public class OkHttpHelper {

    private static final String TAG = OkHttpHelper.class.getSimpleName();

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private static OkHttpHelper sInstance;

    private OkHttpClient mClient;
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private ResultCallback mCallback;
    private Handler mHandler;

    private OkHttpHelper(Context context) {
        mClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                        cookieStore.put(httpUrl.host(), list);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                        List<Cookie> cookies = cookieStore.get(httpUrl.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        mHandler = new Handler(Looper.getMainLooper());
    }

    public static OkHttpHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OkHttpHelper.class) {
                if (sInstance == null) {
                    sInstance = new OkHttpHelper(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void setCallback(ResultCallback callback) {
        this.mCallback = callback;
    }

    public Response get(String url) {
        return get(url, null, false);
    }

    public Response get(String url, HashMap<String, String> headers, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return doRequest(genRequest(url, headers, METHOD_GET, null, forceNetwork));
    }

    public void getAsync(String url) {
        getAsync(url, null, false);
    }

    public void getAsync(String url, HashMap<String, String> headers) {
        getAsync(url, headers, false);
    }

    public void getAsync(String url, HashMap<String, String> headers, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        doRequestAsync(genRequest(url, headers, METHOD_GET, null, forceNetwork));
    }

    /**
     * post form
     * @param url
     * @param params
     * @return
     */
    public Response post(String url, HashMap<String, String> params, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return doRequest(genPostFormRequest(url, params, forceNetwork));
    }

    /**
     * post data
     * @param url
     * @param type MIME type
     * @param file
     * @return
     *
     * MIME reference: http://www.w3school.com.cn/media/media_mimeref.asp
     */
    public Response post(String url, String type, File file, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return doRequest(genPostDataRequest(url, type, file, forceNetwork));
    }

    /**
     * post json
     * @param url
     * @param json
     * @return
     */
    public Response post(String url, JSONObject json, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return doRequest(genPostJsonRequest(url, json, forceNetwork));
    }

    public void postAsync(String url, HashMap<String, String> params, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        doRequestAsync(genPostFormRequest(url, params, forceNetwork));
    }

    public void postAsync(String url, String type, File file, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        doRequestAsync(genPostDataRequest(url, type, file, forceNetwork));
    }

    public void postAsync(String url, JSONObject json, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        doRequestAsync(genPostJsonRequest(url, json, forceNetwork));
    }

    public void uploadAsync(String url, String type, File file, HashMap<String, String> map, boolean forceNetwork) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        doRequestAsync(genPostMultipartRequest(url, type, file, map, forceNetwork));
    }

    public void downloadAsync(String url, File file) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        doRequestAsync(genPostFormRequest(url, null, false), file, null);
    }

    public void downloadAsyncGet(String url, File file) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        doRequestAsync(genRequest(url, null, METHOD_GET, null, false), file, null);
    }

    public void downloadImageAsync(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        doRequestAsync(genRequest(url, null, METHOD_GET, null, false), null, imageView);
    }

    public void cancel(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }

        synchronized (mClient.dispatcher().getClass()) {
            for (Call call : mClient.dispatcher().queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                    Log.d(TAG, "gln_cancel: " + tag);
                }
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                    Log.d(TAG, "gln_cancel: " + tag);
                }
            }
        }
    }

    public void cancelAll() {
        mClient.dispatcher().cancelAll();
    }

    private Request genPostFormRequest(String url, HashMap<String, String> params, boolean forceNetwork) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            Set<String> keys = params.keySet();
            for (String s : keys) {
                builder.add(s, params.get(s));
            }
        }
        return genRequest(url, null, METHOD_POST, builder.build(), forceNetwork);
    }

    private Request genPostDataRequest(String url, String type, File file, boolean forceNetwork) {
        RequestBody body = RequestBody.create(MediaType.parse(type), file);
        return genRequest(url, null, METHOD_POST, body, forceNetwork);
    }

    private Request genPostJsonRequest(String url, JSONObject json, boolean forceNetwork) {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(type, json.toString());
        return genRequest(url, null, METHOD_POST, requestBody, forceNetwork);
    }

    private Request genPostMultipartRequest(String url, String type, File file, HashMap<String, String> map, boolean forceNetwork) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        if (map != null && !map.isEmpty()) {
            Set<String> keys = map.keySet();
            for (String s: keys) {
                builder.addFormDataPart(s, map.get(s));
            }
        }

        builder.addPart(RequestBody.create(MediaType.parse(type), file));
        return genRequest(url, null, METHOD_POST, builder.build(), forceNetwork);
    }

    private Request genRequest(String url, HashMap<String, String> headers, String method, RequestBody requestBody, boolean forceNetwork) {
        Request.Builder builder = new Request.Builder().url(url);
        builder.tag(url);
        if (headers != null && !headers.isEmpty()) {
            Set<String> keys = headers.keySet();
            for (String s : keys) {
                builder.header(s, headers.get(s));
            }
        }

        if (forceNetwork) {
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        if ("GET".equals(method)) {
            builder.method(method, null);
        } else {
            builder.method(method, requestBody);
        }

        return builder.build();
    }

    private Response doRequest(Request request) {
        Call call = mClient.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void doRequestAsync(Request request) {
        doRequestAsync(request, null, null);
    }

    private void doRequestAsync(Request request, final File file, final ImageView imageView) {
        Call call = mClient.newCall(request);
        Callback callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.w(TAG, "gln_onFailure[call: " + call + ", IOException: " + e + "]");
                    final IOException finalE = e;
                    final Call finalCall = call;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.onFailure(finalCall, finalE);
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != response.cacheResponse()) {
                        Log.d(TAG, "gln_onResponse cache[call: " + call + ", response: " + response.cacheResponse().toString() + "]");
                    } else {
                        Log.d(TAG, "gln_onResponse network[call: " + call + ", response: " + response.networkResponse().toString() + "]");
                    }
                    final Response finalResponse = response;
                    final Call finalCall = call;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.onResponse(finalCall, finalResponse);
                            }
                        }
                    });

                    if (file != null) {
                        InputStream in = response.body().byteStream();
                        byte[] b = new byte[1024];
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(file);
                            int len;
                            while ((len = in.read(b)) != -1) {
                                out.write(b, 0, b.length);
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (in != null)
                                in.close();
                            if (out != null)
                                out.close();
                        }
                    }

                    if (imageView != null) {
                        InputStream in = response.body().byteStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(in);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            };
        call.enqueue(callback);
    }

    public static abstract class ResultCallback {
        public abstract void onFailure(Call call, IOException e);
        public abstract void onResponse(Call call, Response response);
    }

    public static class MIMEType {
        public static final String doc = "application/msword; charset=utf-8";
        public static final String xls = "application/vnd.ms-excel; charset=utf-8";
        public static final String ppt = "application/vnd.ms-powerpoint; charset=utf-8";
        public static final String wps = "application/vnd.ms-works; charset=utf-8";
        public static final String pdf = "application/pdf; charset=utf-8";

        // class, bin, exe
        public static final String exe = "application/octet-stream; charset=utf-8";

        public static final String zip = "application/zip; charset=utf-8";

        public static final String mp3 = "audio/mpeg";
        public static final String wav = "audio/x-wav";

        public static final String bmp = "image/bmp";
        public static final String gif = "image/gif";
        public static final String jpg = "image/jpeg"; // jpe, jpeg, jpg
        public static final String ico = "image/x-icon";
        public static final String rgb = "image/x-rgb";

        public static final String js = "application/x-javascript; charset=utf-8";
        public static final String css = "text/css";
        public static final String html = "text/html"; //htm, html, stm
        public static final String txt = "text/plain";  // bas, c, h, txt

        public static final String avi = "video/x-msvideo";
    }
}
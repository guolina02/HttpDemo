package com.guolina.httpdemo.okhttp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.guolina.httpdemo.R;
import com.guolina.httpdemo.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by guolina on 2017/5/25.
 */
public class OkHttpActivity extends Activity implements View.OnClickListener {

    private static final String TAG = OkHttpActivity.class.getSimpleName();

    private ToggleButton mToggleButton;
    private boolean mAsync;

    private OkHttpHelper.ResultCallback mCallback = new MyCallback();
    private OkHttpHelper mOkHttpHelper;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okhttp);

        findViewById(R.id.btn_okhttp_get).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_post_form).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_post_json).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_post_upload).setOnClickListener(this);
        findViewById(R.id.btn_okhttp_post_download).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);

        mToggleButton = (ToggleButton) findViewById(R.id.toggle_async);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAsync = isChecked;
            }
        });

        mOkHttpHelper = OkHttpHelper.getInstance(this);
        mOkHttpHelper.setCallback(mCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAsync = mToggleButton.isChecked();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            cancelRequest();
            return;
        }

        if (mAsync) {
            switch (v.getId()) {
                case R.id.btn_okhttp_get:
                    mOkHttpHelper.getAsync("https://www.baidu.com/s?ie=utf-8&wd=age");
                    break;
                case R.id.btn_okhttp_post_form:
                    HashMap<String, String> params = new HashMap<>();
                    params.put("ie", "utf-8");
                    params.put("wd", "age");
                    mOkHttpHelper.postAsync("https://www.baidu.com", params, false);
                    break;
                case R.id.btn_okhttp_post_json:
                    JSONObject json = new JSONObject();
                    try {
                        json.put("ie", "utf-8");
                        json.put("wd", "age");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mOkHttpHelper.postAsync("https://www.baidu.com", json, false);
                    break;
                case R.id.btn_okhttp_post_upload:
                    mOkHttpHelper.postAsync("https://github.com/guolina02/HttpDemo", OkHttpHelper.MIMEType.txt, FileUtils.getOkHttpUploadFile(), false);
                    break;
                case R.id.btn_okhttp_post_download:
                    mOkHttpHelper.postAsync("https://github.com/guolina02/HttpDemo/blob/master/README.md", new HashMap<String, String>(), false);
                    break;
            }
        } else {
            new MyAsyncTask().execute(v.getId());
        }
    }

    private void onFailure(String url) {
        Toast.makeText(OkHttpActivity.this, "gln_onFailure[url: " + url + "]", Toast.LENGTH_SHORT).show();
    }

    private void onResponse(String url, Response response) {
        Toast.makeText(OkHttpActivity.this, "gln_onResponse[url: " + url + "]", Toast.LENGTH_SHORT).show();
        if (!TextUtils.isEmpty(url) && "".equals(url)) {
            byte[] bytes = new byte[1024];
            InputStream inStream = response.body().byteStream();
            int result;
            File file = new File(FileUtils.getOkHttpDownloadPath() + "/okhttp/" + url);
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
                while ((result = inStream.read(bytes)) != -1) {
                    outStream.write(bytes, 0, bytes.length);
                }
            } catch (IOException e) {
                Toast.makeText(OkHttpActivity.this, "gln_onResponse file save exception", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            Toast.makeText(OkHttpActivity.this, "gln_onResponse file save success!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRequest() {
        final String url = "https://www.baidu.com/s?ie=utf-8&wd=age";
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOkHttpHelper.cancel(url);
            }
        }, 1);
        mOkHttpHelper.getAsync(url);
    }

    private class MyCallback extends OkHttpHelper.ResultCallback {

        private WeakReference<OkHttpActivity> mWeakReference;

        MyCallback() {
            mWeakReference = new WeakReference<OkHttpActivity>(OkHttpActivity.this);
        }

        @Override
        public void onFailure(Call call, IOException e) {
            mWeakReference.get().onFailure(call.request().tag().toString());
        }

        @Override
        public void onResponse(Call call, Response response) {
            mWeakReference.get().onResponse(call.request().url().toString(), response);
        }
    }

    private class MyAsyncTask extends AsyncTask<Integer, Void, Response> {

        private String url;

        @Override
        protected Response doInBackground(Integer... params) {
            int id = params[0];
            Response response = null;
            switch (id) {
                case R.id.btn_okhttp_get:
                    url = "https://www.baidu.com/s?ie=utf-8&wd=age";
                    response = mOkHttpHelper.get(url);
                    break;
                case R.id.btn_okhttp_post_form:
                    HashMap<String, String> param = new HashMap<>();
                    param.put("ie", "utf-8");
                    param.put("wd", "age");
                    url = "https://www.baidu.com";
                    response = mOkHttpHelper.post(url, param, false);
                    break;
                case R.id.btn_okhttp_post_json:
                    JSONObject json = new JSONObject();
                    try {
                        json.put("ie", "utf-8");
                        json.put("wd", "age");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    url = "https://www.baidu.com";
                    response = mOkHttpHelper.post(url, json, false);
                    break;
                case R.id.btn_okhttp_post_upload:
                    url = "https://github.com/guolina02/HttpDemo";
                    response = mOkHttpHelper.post(url, OkHttpHelper.MIMEType.txt, FileUtils.getOkHttpUploadFile(), false);
                    break;
                case R.id.btn_okhttp_post_download:
                    url = "https://github.com/guolina02/HttpDemo/test.txt";
                    response = mOkHttpHelper.post(url, new HashMap<String, String>(), false);
                    break;
            }
            return response;
        }

        @Override
        protected void onPostExecute(Response s) {
            if (s == null || !s.isSuccessful()) {
                Log.d(TAG, "gln_onFailure[s: " + (s == null ? "null" : s.toString()));
                onFailure(url);
            } else {
                if (null != s.cacheResponse()) {
                    Log.d(TAG, "gln_onResponse cache[response: " + s.cacheResponse().toString() + "]");
                } else {
                    Log.d(TAG, "gln_onResponse network[response: " + s.networkResponse().toString() + "]");
                }
                onResponse(url, s);
            }
        }
    }
}

package com.guolina.httpdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.guolina.httpdemo.okhttp.OkHttpActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnOkHttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnOkHttp = (Button) findViewById(R.id.btn_okhttp);
        mBtnOkHttp.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_okhttp:
                intent.setClass(MainActivity.this, OkHttpActivity.class);
                break;
        }
        startActivity(intent);
    }
}

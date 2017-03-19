package com.hello2mao.xlogging.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hello2mao.xlogging.okhttp3.XLoggingInterceptor;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.okhttp3_get)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okhttp3_get:
                okhttp3Get();
                break;
            default:
                break;
        }
    }

    private void okhttp3Get() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new XLoggingInterceptor())
                .build();
        Request request = new Request.Builder()
                .url("http://news-at.zhihu.com/api/4/news/latest")
                .method("GET", null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("test", "responseBody=" + responseBody);
                } else {
                    Log.e("test", "okhttp3Get failed");
                }
            }
        });
    }
}

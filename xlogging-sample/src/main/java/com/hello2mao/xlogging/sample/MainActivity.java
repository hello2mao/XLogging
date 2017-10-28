package com.hello2mao.xlogging.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.hello2mao.xlogging.TransactionData;
import com.hello2mao.xlogging.XLogging;
import com.hello2mao.xlogging.XLoggingCallback;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;
import com.hello2mao.xlogging.sample.httpclient.HttpClientUtil;
import com.hello2mao.xlogging.sample.okhttp.OkHttpUtil;
import com.hello2mao.xlogging.sample.urlconnection.URLConnectionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String RES_SCHEME = "https";
    public static final String RES_HOST = "image.baidu.com";
    public static final String RES_PATH = "/channel/listjson";
    public static final String RES_QUERY = "?pn=0&rn=10&tag1=美女&tag2=全部&ftags=校花&ie=utf8";
    public static final String RES_URL = RES_SCHEME + "://" + RES_HOST + RES_PATH + RES_QUERY;

    @BindView(R.id.iv_pic)
    ImageView ivPic;
    @BindView(R.id.URLConnection)
    RadioButton rbURLConnection;
    @BindView(R.id.OkHttp)
    RadioButton rbOkHttp;
    @BindView(R.id.HttpClient)
    RadioButton rbHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        // 安装XLogging
//        XLogging.install();
        // 安装带回调的XLogging
        XLogging.install(new XLoggingCallback() {
            @Override
            public void handle(TransactionData transactionData) {
                System.out.println(transactionData.toString());
            }
        });


        showPic();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.show_pic)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_pic:
                showPic();
                break;
            default:
                break;
        }
    }

    private void showPic() {
        if (rbURLConnection.isChecked()) {
            URLConnectionUtil.showPic();
        } else if (rbOkHttp.isChecked()) {
            OkHttpUtil.showPic(getApplicationContext(), ivPic);
        } else if (rbHttpClient.isChecked()) {
            HttpClientUtil.showPic();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaiduImageBean baiduImageBean) {
        // 主线程更新UI，使用Eventbus
//        Glide.with(getApplicationContext())
//                .load(baiduImageBean.getData().get(new Random().nextInt(10)).getImage_url())
//                .crossFade()
//                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                .into(ivPic);
    }
}

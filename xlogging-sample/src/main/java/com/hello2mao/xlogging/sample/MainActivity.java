package com.hello2mao.xlogging.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;
import com.hello2mao.xlogging.sample.http.RetrofitHelper;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_pic)
    ImageView ivPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        showPic();
    }

    @OnClick(R.id.okhttp3_get)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okhttp3_get:
                showPic();
                break;
            default:
                break;
        }
    }

    private void showPic() {
        // http://image.baidu.com/channel/listjson?pn=0&rn=30&tag1=美女&tag2=全部&ftags=校花&ie=utf8
        RetrofitHelper.getInstance().fetchBaiduImageInfo(0, 3, "美女", "全部", "校花", "utf8")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaiduImageBean>() {
                    @Override
                    public void call(BaiduImageBean baiduImageBean) {
                        Glide.with(getApplicationContext())
                                .load(baiduImageBean.getData().get(new Random().nextInt(3)).getImage_url())
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(ivPic);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }, new Action0() {
                    @Override
                    public void call() {

                    }
                });
    }
}

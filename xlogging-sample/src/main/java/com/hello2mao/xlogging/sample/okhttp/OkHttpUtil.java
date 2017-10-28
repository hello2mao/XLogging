package com.hello2mao.xlogging.sample.okhttp;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;

import java.util.Random;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class OkHttpUtil {

    public static void showPic(final Context context, final ImageView imageView) {
        // https://image.baidu.com/channel/listjson?pn=0&rn=10&tag1=美女&tag2=全部&ftags=校花&ie=utf8
        RetrofitHelper.getInstance().fetchBaiduImageInfo(0, 10, "美女", "全部", "校花", "utf8")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaiduImageBean>() {
                    @Override
                    public void call(BaiduImageBean baiduImageBean) {
                        Glide.with(context)
                                .load(baiduImageBean.getData().get(new Random().nextInt(10)).getImage_url())
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(imageView);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        // ignore
                    }
                });
    }
}

package com.hello2mao.xlogging.sample.okhttp.api;


import com.hello2mao.xlogging.sample.MainActivity;
import com.hello2mao.xlogging.sample.bean.BaiduImageBean;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface BaiduImageApis {

    String BASE_URL = MainActivity.RES_SCHEME + "://" + MainActivity.RES_HOST;

    // https://image.baidu.com/channel/listjson?pn=0&rn=30&tag1=美女&tag2=全部&ftags=校花&ie=utf8
    // 返回格式为json
    // pn为第几页
    // rn为一页返回的图片数量
    @GET("channel/listjson")
    Observable<BaiduImageBean> getBaiduImageInfo(@Query("pn") int pn,
                                                 @Query("rn") int rn,
                                                 @Query("tag1") String tag1,
                                                 @Query("tag2") String tag2,
                                                 @Query("ftags") String ftags,
                                                 @Query("ie") String ie);
}

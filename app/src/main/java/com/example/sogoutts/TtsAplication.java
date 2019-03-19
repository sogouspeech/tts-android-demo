// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.example.sogoutts;

import android.support.multidex.MultiDexApplication;

import com.sogou.tts.TTSPlayer;
import com.sogou.tts.ZhiyinInitInfo;

public class TtsAplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ZhiyinInitInfo.Builder builder = new ZhiyinInitInfo.Builder();
        // 传入从知音平台申请获得的域名，从知音平台获取的appid，设备的uuid。如果需要刷新token则传入appkey，否则传入token
//        ZhiyinInitInfo info = builder.setBaseUrl("").setUuid("").setAppid("").setAppkey("").setToken("").create();
//        ZhiyinInitInfo info = builder.setBaseUrl("canary.speech.sogou.com").setUuid("aabb").setAppid("1DscivQKSL3rncPyvTnGsLdh4YD").setAppkey("0jxC05r2jrXTkJBr3WhBYEuJzZLVTE6KSsyvIljNbSYeE0qGv3rvCL7OgGHaOlkoTRia6jQGMGGpnZKwhTJ4ZA==").create();

//        TTSPlayer.initZhiyinInfo(this,info);
    }
}
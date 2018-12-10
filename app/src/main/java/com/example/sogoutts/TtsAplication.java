// Copyright 2018 Sogou Inc. All rights reserved. 
// Use of this source code is governed by the Apache 2.0 
// license that can be found in the LICENSE file. 
package com.example.sogoutts;

import android.support.multidex.MultiDexApplication;

import com.sogou.tts.TTSPlayer;

public class TtsAplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // 传入从知音平台申请的域名，不需要scheme，例如*.*.sogou.com
        TTSPlayer.initAuth(this,"");
    }
}
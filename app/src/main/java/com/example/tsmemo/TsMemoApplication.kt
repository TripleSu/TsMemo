package com.example.tsmemo

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import io.realm.Realm

class TsMemoApplication() : Application(){
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        NaverMapSdk.getInstance(this).setClient(NaverMapSdk.NaverCloudPlatformClient("ktprfbaj89"))
    }
}
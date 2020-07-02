package com.example.tsmemo.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class MemoData (   // * RealmObject는 반드시 open 이어야 함
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),  // 메모의 고유 ID KEY, 초기값인 UUID는 랜덤한 고유값을 자동으로 생성함
    var createdAt: Date = Date(),                   // 작성시간
    var title: String = "",                         // 제목
    var content: String = "",                       // 내용
    var summary: String = "",                       // 내용요약
    var imageFile: String = "",                     // 첨부이미지 파일이름
    var latitude: Double = 0.0,                     // 위도
    var longitude: Double = 0.0,                    // 경도
    var alarmTime: Date = Date(),                   // 알람시간
    var weather: String = ""                        // 날씨
) : RealmObject()   // * RealmObject를 상속

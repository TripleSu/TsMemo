package com.example.tsmemo.data

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class MemoDao(private val realm: Realm){
    fun getAllMemos() : RealmResults<MemoData> {    // 전체 메모리스트
        return realm.where(MemoData::class.java)
            .sort("createdAt", Sort.DESCENDING)
            .findAll()
    }

    fun selectMemo(id: String): MemoData {      // 지정 메모 검색
        return realm.where(MemoData::class.java)
            .equalTo("id", id)
            .findFirst() as MemoData
    }

    // * 전체 MemoData중 alarmTime이 현재시간(Date()) 보다 큰 데이터만 가져오는 함수
    fun getActiveAlarms(): RealmResults<MemoData> {
        return realm.where(MemoData::class.java)
            .greaterThan("alarmTime", Date())
            .findAll()
    }

    // to-be
    fun addOrUpdateMemo(memoData: MemoData){
        realm.executeTransaction{
            memoData.createdAt = Date()

            if(memoData.content.length > 100){
                memoData.summary = memoData.content.substring(0..100)
            } else {
                memoData.summary = memoData.content
            }

                it.copyToRealmOrUpdate(memoData)
            }
        }
}

    // as-is
    /*
    fun addOrUpdateMemo(memoData: MemoData, title: String, content: String, alarmTime: Date){
        realm.executeTransaction{
            memoData.title = title
            memoData.content = content
            memoData.createdAt = Date()
            memoData.alarmTime = alarmTime

            if(content.length > 100){
                memoData.summary = content.substring(0..100)
            } else {
                memoData.summary = content
            }

            if(!memoData.isManaged){    // * Managed 상태가 아닌 경우 copyToRealm() 함수로 DB에 추가
                it.copyToRealmOrUpdate(memoData)
            }
        }
    }
    */

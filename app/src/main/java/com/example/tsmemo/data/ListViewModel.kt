package com.example.tsmemo.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm

class ListViewModel : ViewModel(){
    private val realm: Realm by lazy {      // * Realm 인스턴스를 생성하여 사용하는 변수
        Realm.getDefaultInstance()
    }

    private val memoDao: MemoDao by lazy {  // * Realm 인스턴스를 넣어 MemoDao를 생성하여 사용하는 변수
        MemoDao(realm)
    }

    val memoLiveData: RealmLiveData<MemoData> by lazy {
        RealmLiveData<MemoData> (memoDao.getAllMemos()) // * MemoDao 에서 모든 메모를 가져와서 RealmLiveData로 변환하여 사용하는 변수
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()   // * LiveViewModel을 더이상 사용하지 않을 때 Realm 인스턴스를 닫음
    }
    /*
    ■ DB연동 전 소스
    private val memos: MutableList<MemoData> = mutableListOf()
    val memoLiveData: MutableLiveData<MutableList<MemoData>> by lazy {
        MutableLiveData<MutableList<MemoData>>().apply { // *MutableList를 담을 MutableLiveData를 추가 (성능을 위해서 lazy를 사용하여 지연 초기화)
            value = memos
        }
    }


    /*
        메모(MemoData 객체)를 리스트에 추가하고 MutableLiveData의 value를 갱신하여 Observer를 호출하도록 하는 함수
     */
    fun addMemo(data: MemoData) {
        val tempList = memoLiveData.value
        tempList?.add(data)
        memoLiveData.value = tempList
    }

     */
}
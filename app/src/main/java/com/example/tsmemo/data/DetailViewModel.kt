package com.example.tsmemo.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsmemo.AlarmTool
import io.realm.Realm
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

// * DetailActivity에서 사용하는 ViewModel 입니다.

class DetailViewModel: ViewModel(){ // * ViewModel 상속

    /*
    val title: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val content: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val alarmTime: MutableLiveData<Date> = MutableLiveData<Date>().apply { value = Date(0) } // * 알람 시간을 가져와서 갱신하는 변수

    private var memoData = MemoData()   // * MemoData를 저장할 때 사용할 변수를 선언
    */
    var memoData = MemoData()
    val memoLiveData: MutableLiveData<MemoData> by lazy {
        MutableLiveData<MemoData>().apply { value = memoData}
    }

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val memoDao: MemoDao by lazy {
        MemoDao(realm)
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    fun loadMemo(id: String) {  // * 메모 load
        memoData = realm.copyFromRealm(memoDao.selectMemo(id))
        memoLiveData.value = memoData
    }

    fun addOrUpdateMemo(context: Context){    // * memo Update
        memoDao.addOrUpdateMemo(memoData)

        // * AlarmTool을 통해 메모와 연결된 기존 알람정보를 삭제하고 새 알람시간이 현재시간 이후라면 새로 등록
        AlarmTool.deleteAlarm(context, memoData.id)
        if(memoData.alarmTime.after(Date())) {
            AlarmTool.addAlarm(context, memoData.id, memoData.alarmTime)
        }
    }

    fun deleteAlarm() {
        memoData.alarmTime = Date(0)
        memoLiveData.value = memoData
    }

    fun setAlarm(time: Date) {
        memoData.alarmTime = time
        memoLiveData.value = memoData
    }

    fun deleteLocation() {
        memoData.latitude = 0.0
        memoData.longitude = 0.0
        memoLiveData.value = memoData
    }

    fun setLocation(latitude: Double, longitude: Double) {
        memoData.latitude = latitude
        memoData.longitude = longitude
        memoLiveData.value = memoData
    }

    fun deleteWeather() {
        memoData.weather = ""
        memoLiveData.value = memoData
    }

    fun setWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            memoData.weather = WeatherData.getCurrentWeather(latitude, longitude)
            memoLiveData.value = memoData
        } // * viewmodelScope는 viewModel이 소멸할 때에 맞춰 코루틴을 정지시켜 줌
    }

    // * 이미지를 받아 설정하는 함수 (Context도 받아야됨)
    fun setImageFile(context: Context, bitmap: Bitmap) {
        val imageFile = File(context.getDir("image", Context.MODE_PRIVATE), memoData.id + ".jpg")
        if(imageFile.exists()) imageFile.delete()

        try{
            imageFile.createNewFile()
            val outputStream = FileOutputStream(imageFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()

            memoData.imageFile = memoData.id + ".jpg"
            memoLiveData.value = memoData
        } catch (e: Exception){
            println(e)
        }
    }
}
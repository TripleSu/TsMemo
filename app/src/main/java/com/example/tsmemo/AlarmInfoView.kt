package com.example.tsmemo

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.view_info.view.*
import java.text.SimpleDateFormat
import java.util.*


class AlarmInfoView @JvmOverloads constructor (context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0)
    : InfoView(context, attrs, defStyleAttr) {
    companion object { // * 클래스 공용부인 companion object 안에 알람의 시간 표시형식을 만듬
        private val dateFormat = SimpleDateFormat("yy/MM/dd HH:mm")
    }

    init{   // * 클래스 초기화
        typeImage.setImageResource(R.drawable.ic_alarm) // view에 표시할 초기값 지정
        infoText.setText("")
    }

    fun setAlarmDate(alarmDate: Date){
        if(alarmDate.before(Date())) {
            infoText.setText("알람이 없습니다")
        } else {
            infoText.setText(dateFormat.format(alarmDate))
        }
    }
}

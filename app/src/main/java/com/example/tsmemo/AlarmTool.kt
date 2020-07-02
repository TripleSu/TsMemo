package com.example.tsmemo


import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tsmemo.data.MemoDao
import io.realm.Realm
import java.util.*


class AlarmTool: BroadcastReceiver() { // * BroadcastReceiver 상속 받음
    companion object
    {
        private const val ACTION_RUN_ALARM = "RUN_ALARM" // * 알람 Intent를 분류 action 상수 선언

        private fun createAlarmIntent(context: Context, id: String): PendingIntent {    // * 알람으로 보낼 Intent를 생성
            val intent = Intent(context, AlarmTool::class.java) // * AlarmTool 클래스를 목적지로 하는 Intent 생성(Receiver 역활도 하시 때문)
            intent.data = Uri.parse("id:" + id)
            intent.putExtra("MEMO_ID", id)
            intent.action = ACTION_RUN_ALARM

            return PendingIntent.getBroadcast(context, 0, intent, 0) // * intent를 PendingIntent의 Broadcast 형태로 만들어 반환함
        }

        fun addAlarm(context:Context, id: String, alarmTime: Date) { // * 알람 설정
            val alarmIntent = createAlarmIntent(context, id)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.time, alarmIntent)
        }

        fun deleteAlarm(context: Context, id: String) { // * 알람 삭제
            val alarmIntent = createAlarmIntent(context, id)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(alarmIntent)
        }
    }

    // * BroadcastReceiver 가 broadcast 를 받았을 때 동작 함수
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) // * intent 안에 설정된 action 값으로 intent의 종류를 분류함
        {
            AlarmTool.ACTION_RUN_ALARM -> {
                val memoId = intent.getStringExtra("MEMO_ID")
                val realm = Realm.getDefaultInstance()
                val memoData = MemoDao(realm).selectMemo(memoId)

                val notificationIntent = Intent(context, DetailActivity::class.java) // * Notification을 누르면 해당 메모의 상세화면으로 이동하도록 만들어 줌
                notificationIntent.putExtra("MEMO_ID", memoId)

                val pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT) // * intent를 PendingIntent의 Activity 형태로 만들어 반환함

                val builder = NotificationCompat.Builder(context, "alarm") // Channel ID를 alarm 으로 지정
                    .setContentTitle(memoData.title)
                    .setContentText(memoData.content)
                    .setContentIntent(pendingIntent) // * 클릭시 상세화면으로 이동하도록 pendingIntent 를 연결시킴
                    .setAutoCancel(true) // * 클릭시 notification이 사라지도록 AutoCancel 값을 true로 지정

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // * 시스템의 NotificationManager를 받아옴
                // * 안드로이드 버전에 따라 분기
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // * OREO 이상의 버전에서는 NotificationChannel 필수 코드
                    builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    val channel = NotificationChannel("alarm", "알람 메세지",
                        NotificationManager.IMPORTANCE_HIGH) // * 앞서 지정한 alarm 이라는 채널 이름 "알람 메세지" 그리고 중요도 높음으로 지정하여 NotificationManager 등록

                    // * 여러개의 createNotificationChannel을 사용자에게 제공하여 채널별로 선택적으로 사용 또는 차단하게 하는 기능
                    notificationManager.createNotificationChannel(channel)
                } else {
                    builder.setSmallIcon(R.mipmap.ic_launcher)  // 아이콘 설정
                }

                notificationManager.notify(1, builder.build()) // * 첫번째 패러미터인 id값을 변경하면 여러개를 띄울 수 있음음
            }

            // * 기기 부탕시 받을 수 있는 broadcast의 action인 Intent.ACTION_BOOT_COMPLETED를 when의 분기로 추가함함
            Intent.ACTION_BOOT_COMPLETED ->
            {
                val realm = Realm.getDefaultInstance()
                val activeAlarms = MemoDao(realm).getActiveAlarms()

                for(memoData in activeAlarms) {
                    addAlarm(context, memoData.id, memoData.alarmTime)
                }
            }
       }
    }
}
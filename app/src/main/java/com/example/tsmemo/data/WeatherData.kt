package com.example.tsmemo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.net.URL


class WeatherData {
//test999
    companion object {
        // * xml을 파싱하기 위한 객체 생성
        private val xmlPullParserFactory by lazy { XmlPullParserFactory.newInstance() }

    // * 위치 정보를 받아 현재 날씨를 문자열로 반환하는 함수
    // * suspend 코루틴에서 언제든지 중지 또는 재개할 수 있도록 함
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): String {
        // * 함수 내용은 네트웍으로 처리되므로 코루틴을 사용하여 IO 쓰레드에서 동작하도록 함 ( 이렇게 하지 않을 경우 Exception 발생 )
        return GlobalScope.async(Dispatchers.IO) {
            val requestUrl = "https://api.openweathermap.org/data/2.5/weather" +
                "?lat=${latitude}&lon=${longitude}&mode=xml&units=metric&" +
                "appid=a485e02893e86caa92024db7f4b7c25f" // * 날씨 정보 api 주소에 위도와 경도 넣어 문자열로 만들어 줌

            var currentWeather = "" // * 결과값인 날씨(문자열)를 저장하는 변수

            try{
                val url = URL(requestUrl)
                val stream = url.openStream()
                val parser = xmlPullParserFactory.newPullParser()
                parser.setInput(InputStreamReader(stream, "UTF-8"))
                var eventType = parser.eventType
                var currentWeatherCode = 0  // * 결과 xml 에 담긴 날씨 코드를 담을 변수

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG && parser.name == "weather") {
                        currentWeatherCode = parser.getAttributeValue(null, "number").toInt()
                        break
                    }
                    eventType = parser.next()
                }

                when( currentWeatherCode ) {
                    in 200..299 -> currentWeather = "뇌우"
                    in 300..399 -> currentWeather = "이슬비"
                    in 500..599 -> currentWeather = "비"
                    in 600..699 -> currentWeather = "눈"
                    in 700..761 -> currentWeather = "안개"
                    800 -> currentWeather = "맑음"
                    in 801..802 -> currentWeather = "구름조금"
                    in 803..804 -> currentWeather = "구름많음"
                    else -> currentWeather = ""
                }
            } catch (e: Exception) {
                println(e)
            }

            currentWeather // * 코루틴의 반환이므로 변수만 써주면 됨

        }.await()   // * 코루틴의 결과는 await() 함수로 기다린 후 반환함
    }



    }
}
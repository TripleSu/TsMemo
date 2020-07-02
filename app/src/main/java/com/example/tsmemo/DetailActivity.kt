package com.example.tsmemo


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tsmemo.data.DetailViewModel
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.takisoft.datetimepicker.DatePickerDialog
import com.takisoft.datetimepicker.TimePickerDialog
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import java.io.File
import java.lang.Exception
import java.util.*

class DetailActivity : AppCompatActivity() {

    private var viewModel: DetailViewModel? = null
    private val dialogCalendar = Calendar.getInstance()    // * 날짜와 시간 다이얼로그에서 설정 중인 값을 임시로 저장해 두기 위한 변수
    private val REQUEST_IMAGE = 100 // * Intent 로 Activity 결과를 요청할 때 사용하는 요청 코드 값을 추가


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)  // * 기기내에서 이미지 파일을 읽어올 수 있는 ACTION_GET_CONTENT 이용하여 해당 기능이 있는 activity를 호출함
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
            /*
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            */
        }

        viewModel = application!!.let {
            // * viewModel의 생성성
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }

        // * viewModel의 memoLiveData를 observe 하도록 수정
        viewModel!!.memoLiveData.observe(this, Observer {
            supportActionBar?.title = it.title
            contentEdit.setText(it.content)
            alarmInfoView.setAlarmDate(it.alarmTime)
            locationInfoView.setLocation(it.latitude, it.longitude)
            weatherInfoView.setWeather(it.weather)

            val imageFile = File(
                getDir("image", Context.MODE_PRIVATE),
                it.imageFile)

            bgImage.setImageURI(imageFile.toUri())
        })

        // * ListActivity에서 아이템을 선택했을 때 보내주는 메모 id로 데이터를 로드함
        val memoId = intent.getStringExtra("MEMO_ID")
        if (memoId != null) viewModel!!.loadMemo(memoId)

        toolbarLayout.setOnClickListener {
            // * 툴바 레이아웃을 눌렀을 때 제목을 수정하는 다이얼로그를 띄우는 루틴
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)   // * LayoutInflater로 레이아웃 xml을 view 변환
            val titleEdit = view.findViewById<EditText>(R.id.titleEdit)

            AlertDialog.Builder(this)
                .setTitle("제목을 입력하세요")
                .setView(view)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                    supportActionBar?.title = titleEdit.text.toString()
                    toolbarLayout.title = titleEdit.text.toString()
                    viewModel!!.memoData.title = titleEdit.text.toString()
                }).show()
        }

        // * 내용이 변경될 때마다 Listener 내에서 viewModel의 memoData의 내용도 같이 변경해줌
        contentEdit.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.memoData.content = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })


        locationInfoView.setOnClickListener {
            val latitude = viewModel!!.memoData.latitude
            val longitude = viewModel!!.memoData.longitude

            if(!(latitude == 0.0 && longitude == 0.0)) {    // * 좌표가 유효한지 확인
                val mapView = MapView(this) // * 네이버 지도 로드 후 객체를 받은 후에만 옵션의 변경이 가능함
                mapView.getMapAsync{    // * NaverMap 객체를 받기 위해서 getMapAsysnc 함수에 리스너를 설정
                    val latitude = viewModel!!.memoData.latitude
                    val longitude = viewModel!!.memoData.longitude
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
                    it.moveCamera(cameraUpdate)
                }
                AlertDialog.Builder(this)
                    .setView(mapView)
                    .show()
            }
        }
    }

    // * 뒤로가기 누를 때 메모를 DB에 갱신함
    override fun onBackPressed() {
        super.onBackPressed()
        viewModel?.addOrUpdateMemo(this)
    }

    private fun openDateDialog() {  // * 날짜 다이얼로그 call
        val datePickerDialog = DatePickerDialog(this)   // * com.takisoft.datetimepicker.DatePickerDialog import 해야됨
        datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            dialogCalendar.set(year, month, dayOfMonth)
            openTimeDialog()    // * 임시 캘린더 변수에 값을 저장하고 시간을 설정하는 다이얼로그를 열도록 함
        }
        datePickerDialog.show() // * 화면에 띄움
    }

    private fun openTimeDialog() {  // * 시간 다이얼로그 call
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                dialogCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dialogCalendar.set(Calendar.MINUTE, minute)

                viewModel?.setAlarm(dialogCalendar.time)
            },  // * 사용자가 입력한 시간을 임시 캘린더 변수에 설정하고 캘린더 변수의 time값(Date 객체)를 viewModel에 새 알람 값으로 설정해줌
            0, 0, false
        ) // * 다이얼로그의 초기시간은 0시 0분 으로 설정 24시간제 사용 false
        timePickerDialog.show()
    }

    // * activity에서 사용할 메뉴를 설정하는 함수
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)  // * menuInflater 통해 activity의 메뉴로 설정
        return true
    }

    @SuppressLint("MissingPermission") // * 권한을 체크 하지 않는 annotation
    override fun onOptionsItemSelected(item: MenuItem): Boolean {   // * 메뉴 아이템을 선택했을때 실행되는 함수
        when (item.itemId)   // * 선택된 메뉴 id에 따라 분기
        {
            R.id.menu_share -> {    // * 공유 기능
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, supportActionBar?.title)
                intent.putExtra(Intent.EXTRA_TEXT, contentEdit.text.toString())

                startActivity(intent)
            }
            R.id.menu_alarm -> {    // * 알림 설정
                if (viewModel?.memoData?.alarmTime!!.after(Date())) {  // * 기존의 알람값이 현재 시간 기준으로 유효한지 체크
                    AlertDialog.Builder(this)   // * 알람을 재설정 or 삭제 여부 dialog
                        .setTitle("안내")
                        .setMessage("기존에 알람이 설정되어 있습니다. 삭제 또는 재설정할 수 있습니다.")
                        .setPositiveButton("재설정", DialogInterface.OnClickListener { dialog, which ->
                            openDateDialog()    // * 재설정 버튼에서는 날짜 다이얼로그를 띄움
                        })
                        .setNegativeButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                            viewModel?.deleteAlarm()    // * 삭제 버튼에서는 alarmTime을 초기화 함
                        })
                        .show()
                } else {
                    openDateDialog()    // * 알람값이 유효하지 않다면 날짜 다이얼로그를 띄워 알람값을 바로 설정하게 함
                }
            }

            R.id.menu_location -> {
                AlertDialog.Builder(this)
                    .setTitle("안내")
                    .setMessage("현재 위치를 메모에 저장하거나 삭제할 수 있습니다.")
                    .setPositiveButton("위치지정", DialogInterface.OnClickListener { dialog, which ->
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                        if (!isGPSEnabled && !isNetworkEnabled) { // * 위치 기능이 둘 다 꺼진 경우 SnakBar를 띄워 시스템의 위치 옵션화면을 안내
                            Snackbar.make(
                                toolbarLayout,
                                "폰의 위치기능을 켜야 기능을 사용할 수 있습니다.",
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("설정", View.OnClickListener {
                                    val goToSettings = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivity(goToSettings)
                                }).show()
                        } else {    // * Criteria 객체에 위치 정확도와 배터리 소모량을 설정함
                            val criteria = Criteria()
                            criteria.accuracy = Criteria.ACCURACY_MEDIUM
                            criteria.powerRequirement = Criteria.POWER_MEDIUM

                            // * requestSingleUpdate 함수를 이용하여 위치정보를 1회 받아오는 코드
                            locationManager.requestSingleUpdate(criteria, object : LocationListener {
                                    override fun onLocationChanged(location: Location?) {   // * 위치정보를 받아 viewModel에 넘겨줌
                                        location?.run {
                                            viewModel!!.setLocation(latitude, longitude)
                                        }
                                    }

                                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                    }

                                    override fun onProviderEnabled(provider: String?) {
                                    }

                                    override fun onProviderDisabled(provider: String?) {
                                    }
                                },null)
                        }
                    })
                    .setNegativeButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                        viewModel!!.deleteLocation()
                    })
                    .show()
            }
            R.id.menu_weather -> {
                AlertDialog.Builder(this)
                    .setTitle("안내")
                    .setMessage("현재 날씨를 메모에 저장하거나 삭제할 수 있습니다.")
                    .setPositiveButton("날씨 가져오기", DialogInterface.OnClickListener { dialog, which ->
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                        if (!isGPSEnabled && !isNetworkEnabled) { // * 위치 기능이 둘 다 꺼진 경우 SnakBar를 띄워 시스템의 위치 옵션화면을 안내
                            Snackbar.make(
                                toolbarLayout,
                                "폰의 위치기능을 켜야 기능을 사용할 수 있습니다.",
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("설정", View.OnClickListener {
                                    val goToSettings =
                                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivity(goToSettings)
                                }).show()
                        } else {    // * Criteria 객체에 위치 정확도와 배터리 소모량을 설정함
                            val criteria = Criteria()
                            criteria.accuracy = Criteria.ACCURACY_MEDIUM
                            criteria.powerRequirement = Criteria.POWER_MEDIUM

                            // * requestSingleUpdate 함수를 이용하여 위치정보를 1회 받아오는 코드
                            locationManager.requestSingleUpdate(criteria, object : LocationListener {
                                override fun onLocationChanged(location: Location?) {   // * 위치정보를 받아 viewModel에 넘겨줌
                                    location?.run {
                                        viewModel!!.setWeather(latitude, longitude)
                                    }
                                }
                                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
                                override fun onProviderEnabled(provider: String?) { }
                                override fun onProviderDisabled(provider: String?) { }
                            },null)
                        }
                    })
                    .setNegativeButton("삭제", DialogInterface.OnClickListener{ dialog, which ->
                        viewModel!!.deleteWeather()
                    })
                    .show()
            }
        }
            return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK){
            try {
                val inputStream = data?.data?.let {contentResolver.openInputStream(it) }
                inputStream?.let {
                    val image = BitmapFactory.decodeStream(it)
                    bgImage.setImageURI(null)
                    image?.let { viewModel?.setImageFile(this, it) }
                    it.close()
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
package com.example.tsmemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tsmemo.data.ListViewModel
import com.example.tsmemo.data.MemoData

import kotlinx.android.synthetic.main.activity_list.*
import java.util.*

class ListActivity : AppCompatActivity() {

    private var viewModel: ListViewModel? = null    // *ListViewModel을 담을 변수를 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        // *MemoListFragment를 화면에 표시
        val fragmentTranstion = supportFragmentManager.beginTransaction()
        fragmentTranstion.replace(R.id.contentLayout, MemoListFragment())
        fragmentTranstion.commit()

        viewModel = application!!.let {  //앱의 객체인 application이 null 인지 먼저 체크함
            /* ViewModel을 가져오기 위해 ViewModelProvider 객체를 생성
                viewModelStore는 ViewModel의 생성과 소멸의 기준
                ViewModelFactory는 ViewModel을 실제로 생성하는 객체
             */
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))    // ViewModel을 가져오기 위해 ViewModelProvider 객체를 생성
                .get(ListViewModel::class.java) // * ViewModelProvider의 get 함수를 통해 ListViewModel을 얻을 수 있음
        }

        fab.setOnClickListener { view ->
            // DetailActivity로 이동하는 코드
            val intent = Intent(applicationContext, DetailActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.example.tsmemo.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tsmemo.R
import kotlinx.android.synthetic.main.item_memo.view.*
import java.text.SimpleDateFormat

class MemoListAdapter (private val list: MutableList<MemoData>): RecyclerView.Adapter<ItemViewHolder> (){

    // *Date 객체를 사람이 볼수 있는 문자열로 변환하기 위한 객체 월/일 시:분으로 출력
    private val dataForamt = SimpleDateFormat("MM/dd HH:mm")

    lateinit var itemClickListener: (itemId: String) -> Unit    // * itemClickListener 변수 추가

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memo, parent, false)
        view.setOnClickListener{    // * 아이템이 클릭될 때 view의 tag에서 메모 id를 받아서 리스너에 넘김 (tag는 아래서 추가)
            itemClickListener?.run{
                val memoId = it.tag as String
                this(memoId)
            }
        }
        return ItemViewHolder(view) // *item_memo를 불러 viewHolder를 생성함
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        /*
            *참고 : View의 visibility의 종류
            * VISIBLE   : View를 화면에 표시
            * INVISIBLE : View의 내용만 감추고 영역은 유지
            * GONE      : View의 내용 및 영역까지 제거
         */
        if(list[position].title.isNotEmpty()){
            // *제목이 있는 경우 titleView를 화면에 표시(VISBLE)하고 title 값을 할당하여 보여줌
            holder.containerView.titleView.visibility = View.VISIBLE
            holder.containerView.titleView.text = list[position].title
        } else {
            // *제목이 없는 경우 titleView의 영역까지 숨겨줌(GONE)
            holder.containerView.titleView.visibility = View.GONE
        }

        holder.containerView.summaryView.text = list[position].summary
        holder.containerView.dateView.text = dataForamt.format(list[position].createdAt)
        holder.containerView.tag = list[position].id // * 아이템 view에 메모의 id를 설정
    }

}
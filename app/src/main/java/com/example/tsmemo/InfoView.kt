package com.example.tsmemo

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

open class InfoView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    init {
        inflate(context, R.layout.view_info, this) // * init 내에서 view의 inflate 함수를 사용하여 view_info.xml을 내부에 포함시킴
    }
}

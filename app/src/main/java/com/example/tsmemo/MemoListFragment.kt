package com.example.tsmemo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tsmemo.data.ListViewModel
import com.example.tsmemo.data.MemoListAdapter
import kotlinx.android.synthetic.main.fragment_memo_list.*

/**
 * A simple [Fragment] subclass.
 */
class MemoListFragment : Fragment() {

    // * MemoListAdapter와 ListViewModel을 담을 속성을 선언
    private lateinit var listAdapter: MemoListAdapter
    private var viewModel: ListViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memo_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,  // * activity 속성은 ListActivity를 가리키는 것, activity의 viewModelStore를 쓰는 이유는 activity와 viewModel을 공유할 수 있기 때문
                                            // fragment의 viewModelStore를 사용한다면 MemoListFragment만의 viewModel이 따로 생성됨
                ViewModelProvider.AndroidViewModelFactory(it))
                .get(ListViewModel::class.java)

        }

        viewModel!!.let {
            // * MemoLiveData를 가져와서 Adapter에 담아 RecyclerView에 출력하도록 함
            it.memoLiveData.value?.let {
                listAdapter = MemoListAdapter(it)
                memoListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                memoListView.adapter = listAdapter

                listAdapter.itemClickListener = {   // * ListAdapter의 itemClickListener에서 DetailActivity로 이동하도록 함 + 메모 id도 전달
                    val intent = Intent(activity, DetailActivity::class.java)
                    intent.putExtra("MEMO_ID", it)
                    startActivity(intent)
                }
            }

            // * MemoLiveData에 observer 함수를 통해 값이 변할 때 동작할 Observer를 붙여줌(Observer 내에서는 adapter의 갱신 코드를 호출)
            it.memoLiveData.observe(this,
                Observer {
                    listAdapter.notifyDataSetChanged()
                }
            )
        }
    }

    override fun onResume() {   // * memo를 작성 후 리스트 갱신
        super.onResume()
        listAdapter.notifyDataSetChanged()
    }
}

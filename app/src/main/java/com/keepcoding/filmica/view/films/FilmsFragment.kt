package com.keepcoding.filmica.view.films

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.keepcoding.filmica.R
import com.keepcoding.filmica.data.Film
import com.keepcoding.filmica.data.FilmsRepo
import com.keepcoding.filmica.view.util.ItemOffsetDecoration
import kotlinx.android.synthetic.main.fragment_films.*
import kotlinx.android.synthetic.main.layout_error.*

class FilmsFragment : Fragment() {

    lateinit var listener: OnItemClickListener
    var page = 1


    val list: RecyclerView by lazy {
        val instance = view!!.findViewById<RecyclerView>(R.id.list_films)
        instance.addItemDecoration(ItemOffsetDecoration(R.dimen.offset_grid))
        instance.setHasFixedSize(true)
        instance
    }



    val adapter: FilmsAdapter by lazy {
        val instance = FilmsAdapter { film ->
            this.listener.onItemClicked(film)
        }

        instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnItemClickListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_films, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter
        setRecyclerViewScrollListener()
        btnRetry?.setOnClickListener { reload() }
    }

    override fun onResume() {
        super.onResume()
        this.reload()
    }

    fun reload() {
        FilmsRepo.discoverFilms(page,context!!,
            { films ->
                progress?.visibility = View.INVISIBLE
                layoutError?.visibility = View.INVISIBLE
                list.visibility = View.VISIBLE
                adapter.setFilms(films)
            },
            { error ->
                progress?.visibility = View.INVISIBLE
                list.visibility = View.INVISIBLE
                layoutError?.visibility = View.VISIBLE

                error.printStackTrace()
            })
    }


    private fun setRecyclerViewScrollListener() {
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView, newState: Int) {
                super.onScrollStateChanged(list, newState)

                val linearLayout: LinearLayoutManager = list.layoutManager as LinearLayoutManager
                var lastVisibleItemPosition = linearLayout.findLastVisibleItemPosition()
                val totalItemCount = list.layoutManager!!.itemCount

                if(totalItemCount == lastVisibleItemPosition + 1) {
                    loadNewPageDiscover(list)
                    adapter.notifyItemRangeInserted(lastVisibleItemPosition+1, adapter.itemCount)
                }

            }

            private fun loadNewPageDiscover(list: RecyclerView) {
                page += 1

                FilmsRepo.discoverFilms(page, context!!,
                    { films ->
                        progress?.visibility = View.INVISIBLE
                        layoutError?.visibility = View.INVISIBLE
                        list.visibility = View.VISIBLE
                        adapter.setFilms(films)
                    },
                    { error ->
                        progress?.visibility = View.INVISIBLE
                        list.visibility = View.INVISIBLE
                        layoutError?.visibility = View.VISIBLE

                        error.printStackTrace()
                    })
            }

        })
    }


    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }

}
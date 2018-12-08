package com.keepcoding.filmica.view.search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView

import kotlinx.android.synthetic.main.fragment_search.*

import com.keepcoding.filmica.R
import com.keepcoding.filmica.data.FilmsRepo
import com.keepcoding.filmica.view.films.FilmsAdapter
import com.keepcoding.filmica.view.trending.TrendingFragment
import com.keepcoding.filmica.view.util.ItemOffsetDecoration
import kotlinx.android.synthetic.main.layout_error.*


class SearchFragment : Fragment() {


    lateinit var listener: TrendingFragment.OnItemClickListener

    val list: RecyclerView by lazy {
        val instance = view!!.findViewById<RecyclerView>(R.id.list_search_films)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter

        btnRetry?.setOnClickListener {  }

        searchBar.queryHint = "Search film"

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query != null) {
                    searchForFilm(query)
                    progress?.visibility = View.VISIBLE
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }


        })
    }

    private fun searchForFilm(query: String) {

        FilmsRepo.searchFilms(query, context!!,
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

}

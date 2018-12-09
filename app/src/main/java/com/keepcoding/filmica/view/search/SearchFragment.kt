package com.keepcoding.filmica.view.search

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_search.*

import com.keepcoding.filmica.R
import com.keepcoding.filmica.data.Film
import com.keepcoding.filmica.data.FilmsRepo
import com.keepcoding.filmica.view.films.FilmsAdapter
import com.keepcoding.filmica.view.util.ItemOffsetDecoration
import kotlinx.android.synthetic.main.layout_notfound.*


class SearchFragment : Fragment() {


    lateinit var listener: OnItemClickListener
    var query: String = ""

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



    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnItemClickListener) {
            listener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val newQuery = p0?.toString()
                if (newQuery?.length!! > 3){
                    searchForFilm(newQuery)
                    query = newQuery
                    progress?.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        this.searchForFilm(query)
    }


    private fun searchForFilm(query: String = "") {

        FilmsRepo.searchFilms(query, context!!,
            { films ->

                if(films.size == 0){
                    progress?.visibility = View.INVISIBLE
                    list.visibility = View.INVISIBLE
                    layoutNotFound?.visibility = View.VISIBLE
                }else{
                    //Films Found
                    progress?.visibility = View.INVISIBLE
                    list.visibility = View.VISIBLE
                    layoutNotFound?.visibility = View.INVISIBLE
                    adapter.setFilms(films)
                }
            },
            { error ->
                progress?.visibility = View.INVISIBLE
                list.visibility = View.INVISIBLE
                layoutErrorInclude?.visibility = View.VISIBLE

                error.printStackTrace()
            })

    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }

}

package com.keepcoding.filmica.view.watchlist


import android.content.Context
import android.graphics.Color
import android.icu.text.CompactDecimalFormat
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_watchlist.*

import com.keepcoding.filmica.R
import com.keepcoding.filmica.data.Film
import com.keepcoding.filmica.data.FilmsRepo
import com.keepcoding.filmica.view.util.SwipeToDeleteCallback

class WatchlistFragment : Fragment() {

    lateinit var listener: OnItemClickListener

    val adapter: WatchlistAdapter by lazy {
        val instance = WatchlistAdapter{ film ->
            this.listener.onItemClicked(film)
        }
        instance
    }

    lateinit var temporalFilm: Film

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeHandler()
        watchlist.adapter = adapter

    }

    override fun onResume() {
        super.onResume()

        loadWatchlist()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WatchlistFragment.OnItemClickListener) {
            listener = context
        }

    }
    fun loadWatchlist() {
        FilmsRepo.watchlist(context!!) { films ->
            adapter.setFilms(films.toMutableList())
        }
    }

    private fun setupSwipeHandler() {
        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                deleteFilmAt(holder.adapterPosition)

                Snackbar.make(holder.itemView,R.string.film_deleted,Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(context!!,R.color.colorAccent))
                    .setAction(R.string.undo) {
                      restoreFilm()
                    }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(watchlist)
    }

    private fun deleteFilmAt(position: Int) {
        val film = adapter.getFilm(position)
        temporalFilm = film
        FilmsRepo.deleteFilm(context!!, film) {
            adapter.removeFilmAt(position)
        }
    }

    private fun restoreFilm() {
        FilmsRepo.saveFilm(context!!, temporalFilm) {
            loadWatchlist()
        }
    }


    interface OnItemClickListener {
        fun onItemClicked(film: Film)
    }

}

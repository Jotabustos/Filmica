package com.keepcoding.filmica.data

import android.arch.persistence.room.Room
import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object FilmsRepo {

    private val films: MutableList<Film> = mutableListOf()
    private val trendingfilms: MutableList<Film> = mutableListOf()
    private val searchedfilms: MutableList<Film> = mutableListOf()
    private var watchingfilms: MutableList<Film> = mutableListOf()

    @Volatile
    private var db: AppDatabase? = null

    private fun getDbInstance(context: Context): AppDatabase {
        if (db == null) {

            db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "filmica-db"
            ).build()
        }

        return db as AppDatabase
    }

    fun findFilmById(id: String): Film? {

        val filmInDiscover = films.find { film -> film.id == id }
        if (filmInDiscover != null) {
            return filmInDiscover
        }

        val filmInTrending = trendingfilms.find { film -> film.id == id }
        if (filmInTrending != null) {
            return filmInTrending
        }

        val filmInSearch = searchedfilms.find { film -> film.id == id }
        if (filmInSearch != null) {
            return filmInSearch
        }

        val filmInWatchlist = watchingfilms.find { film -> film.id == id }
        if (filmInWatchlist != null) {
            return filmInWatchlist
        }

        return null

    }

    fun discoverFilms(
        context: Context,
        callbackSuccess: ((MutableList<Film>) -> Unit),
        callbackError: ((VolleyError) -> Unit)
    ) {

        if (films.isEmpty()) {
            requestDiscoverFilms(callbackSuccess, callbackError, context)
        } else {
            callbackSuccess.invoke(films)
        }
    }

    fun trendingFilms(
        context: Context,
        callbackSuccess: ((MutableList<Film>) -> Unit),
        callbackError: ((VolleyError) -> Unit)
    ) {

        if (trendingfilms.isEmpty()) {
            requestTrendingFilms(callbackSuccess, callbackError, context)
        } else {
            callbackSuccess.invoke(trendingfilms)
        }
    }

    fun searchFilms(
        query: String,
        context: Context,
        callbackSuccess: ((MutableList<Film>) -> Unit),
        callbackError: ((VolleyError) -> Unit)
    ) {
        searchedfilms.clear()
        requestSearchFilms(query, callbackSuccess, callbackError, context)

    }

    fun saveFilm(
        context: Context,
        film: Film,
        callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().insertFilm(film)
            }

            async.await()
            callbackSuccess.invoke(film)
        }
    }

    fun watchlist(
        context: Context,
        callbackSuccess: (List<Film>) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().getFilms()
            }

            val films: List<Film> = async.await()
            watchingfilms = films as MutableList<Film>
            callbackSuccess.invoke(films)
        }
    }

    fun deleteFilm(
        context: Context,
        film: Film,
        callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().deleteFilm(film)
            }
            async.await()
            callbackSuccess.invoke(film)
        }
    }

    private fun requestDiscoverFilms(
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit,
        context: Context
    ) {
        val url = ApiRoutes.discoverUrl()
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                films.addAll(newFilms)
                callbackSuccess.invoke(films)
            },
            { error ->
                callbackError.invoke(error)
            })

        Volley.newRequestQueue(context)
            .add(request)
    }

    private fun requestTrendingFilms(
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit,
        context: Context
    ) {
        val url = ApiRoutes.trendingUrl()
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                trendingfilms.addAll(newFilms)
                callbackSuccess.invoke(trendingfilms)
            },
            { error ->
                callbackError.invoke(error)
            })

        Volley.newRequestQueue(context)
            .add(request)
    }


    private fun requestSearchFilms(
        query: String,
        callbackSuccess: (MutableList<Film>) -> Unit,
        callbackError: (VolleyError) -> Unit,
        context: Context
    ) {
        val url = ApiRoutes.searchUrl(query)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val newFilms = Film.parseFilms(response)
                searchedfilms.addAll(newFilms)
                callbackSuccess.invoke(searchedfilms)
            },
            { error ->
                callbackError.invoke(error)
            })

        Volley.newRequestQueue(context)
            .add(request)
    }

    fun dummyFilms(): List<Film> {
        return (0..9).map { i ->
            Film(
                title = "Film $i",
                overview = "Overview $i",
                genre = "Genre $i",
                voteRating = i.toDouble(),
                release = "200$i-0$i-0$i"
            )
        }
    }

}
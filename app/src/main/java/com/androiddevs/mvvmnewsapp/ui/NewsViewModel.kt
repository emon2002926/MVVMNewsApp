package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponce
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    app:Application,
    val newsRepository: NewsRepository):AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponce>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponce:NewsResponce?=null

    val searchNews: MutableLiveData<Resource<NewsResponce>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponce:NewsResponce?=null

    init {
        getBreakingNews("us")
    }

     fun getBreakingNews(countryCode:String)=viewModelScope.launch {

         safeBreakingNewsCall(countryCode)
    }

    fun searchNews(searchQuery:String)=viewModelScope.launch {
       safeSearchCall(searchQuery)
    }


    fun handleBreakingNewsResponse(response: Response<NewsResponce>): Resource<NewsResponce> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponce==null){
                    breakingNewsResponce = resultResponse
                }else{
                    val oldArticle = breakingNewsResponce?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(breakingNewsResponce?: resultResponse) // Explicitly return Resource.Success
            }
        }
        return Resource.Error(response.message()) // Return Resource.Error on failure
    }


    fun handleSearchNewsResponse(response: Response<NewsResponce>): Resource<NewsResponce> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponce==null){
                    searchNewsResponce = resultResponse
                }else{
                    val oldArticle = searchNewsResponce?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(searchNewsResponce?: resultResponse) // Explicitly return Resource.Success
            }
        }
        return Resource.Error(response.message()) // Return Resource.Error on failuree
    }

    fun saveArticle(article:Article)=viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun getSavedNews()=newsRepository.getSavedNews()
    fun deleteArticle(article: Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeSearchCall(searchQuery: String){
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.searchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No internet connection"))
            }

        }catch (t: Throwable){

            when(t){
                is IOException->breakingNews.postValue(Resource.Error("Network Failure"))
                else->breakingNews.postValue(Resource.Error("Conversion Error "))
            }

        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getBreakingNews(countryCode,breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No internet connection"))
            }

        }catch (t: Throwable){

            when(t){
                is IOException->breakingNews.postValue(Resource.Error("Network Failure"))
                else->breakingNews.postValue(Resource.Error("Conversion Error "))
            }

        }
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork?:return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)?:return false

            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) ->true
                capabilities.hasTransport(TRANSPORT_CELLULAR) ->true
                capabilities.hasTransport(TRANSPORT_ETHERNET) ->true
                else->false
            }
        }else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI->true
                    TYPE_MOBILE->true
                    TYPE_ETHERNET->true
                    else->false
                }

            }
        }
        return false
    }


}



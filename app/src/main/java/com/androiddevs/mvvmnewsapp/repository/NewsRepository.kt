package com.androiddevs.mvvmnewsapp.repository

import com.androiddevs.mvvmnewsapp.api.RetrofitInstence
import com.androiddevs.mvvmnewsapp.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponce
import retrofit2.Response

class NewsRepository (val db :ArticleDatabase){
    suspend fun getBreakingNews(countryCode:String,pageNumber:Int):Response<NewsResponce>{
       return RetrofitInstence.api.getBreakingNews(countryCode, pageNumber)

    }

    suspend fun searchNews(searchQuery:String,pageNumber: Int):Response<NewsResponce>{
        return RetrofitInstence.api.searchForNews(searchQuery,pageNumber)
    }
    suspend fun upsert(article:Article)=db.getArticleDao().upsert(article)
    fun getSavedNews()= db.getArticleDao().getAllArticle()

    suspend fun deleteArticle(article: Article)=db.getArticleDao().deleteArticle(article)
}
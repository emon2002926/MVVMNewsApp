package com.androiddevs.mvvmnewsapp.models

data class NewsResponce(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)
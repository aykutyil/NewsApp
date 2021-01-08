package com.example.newsapp2.repository

import com.example.newsapp2.api.RetrofitInstance
import com.example.newsapp2.db.ArticleDatabase
import com.example.newsapp2.models.Article

class NewsRepository(
    val db: ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)


    suspend fun getSearchingNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    suspend fun getIntendedNewsSource(searchDomain: String,pageNumber: Int) =
        RetrofitInstance.api.getIntendedNewsSource(searchDomain,pageNumber)

    suspend fun upsert(article:Article) = db.getArticleDao().upsert(article)

    fun getSavedNews() = db.getArticleDao().getAllArticle()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}
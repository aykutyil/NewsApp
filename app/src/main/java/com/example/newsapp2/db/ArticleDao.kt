package com.example.newsapp2.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapp2.models.Article

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article:Article):Long

    @Query("SELECT * FROM articles")
    fun getAllArticle() : LiveData<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewsSource(domainsId:String) : LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)

}

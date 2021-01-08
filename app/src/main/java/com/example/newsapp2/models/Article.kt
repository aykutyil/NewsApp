package com.example.newsapp2.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "articles"
)
data class Article(
    @PrimaryKey(autoGenerate = true)
    val mId:Int? = null, //her haberi kaydetmemek için null başta veriliyor.
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source?,
    val title: String?,
    val url: String?,
    val urlToImage: String?
) :Serializable
//code 1299 SQLITE_CONSTRAINT_NOTNULL hatası verdiğinden dolayı değişkenlere null olabilir ? koyuyoruz. yoksa 3 tane bile kaydetmiyor room
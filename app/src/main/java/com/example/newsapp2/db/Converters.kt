package com.example.newsapp2.db

import androidx.room.TypeConverter
import com.example.newsapp2.models.Source

class Converters {
    @TypeConverter
    fun toSource(name:String) : Source{
        return Source(name,name)
    }

    @TypeConverter
    fun fromSource(source: Source) : String{
        return source.name
    }
}
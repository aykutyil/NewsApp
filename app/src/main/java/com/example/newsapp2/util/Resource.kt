package com.example.newsapp2.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class SuccessState<T>(data: T?) : Resource<T>(data)
    class ErrorState<T>(message: String,data: T?=null) : Resource<T>(data,message)
    class LoadingState<T> : Resource<T>()
}
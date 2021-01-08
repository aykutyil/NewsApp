package com.example.newsapp2.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp2.NewsApplication
import com.example.newsapp2.models.Article
import com.example.newsapp2.models.NewsResponse
import com.example.newsapp2.repository.NewsRepository
import com.example.newsapp2.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    var breakingNewsViewModel: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    var searchNewsViewModel: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    var intendedNewsViewModel: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var intendedNewsPage = 1
    var intendedNewsResponse: NewsResponse? = null

    //News Activity sayesinde viewmodel çağrılır ve init çalışır.
    init {
        getBreakingNews("tr")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }

    fun getSearchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    fun getIntendedNewsSource(newsDomain: String) = viewModelScope.launch {
        safeIntendedNews(newsDomain)
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNewsViewModel.postValue(Resource.LoadingState())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNewsViewModel.postValue(handleBreakingNews(response))
            } else {
                breakingNewsViewModel.postValue(Resource.ErrorState("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNewsViewModel.postValue(Resource.ErrorState("Network Failure"))
                else -> breakingNewsViewModel.postValue(Resource.ErrorState("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNewsViewModel.postValue(Resource.LoadingState())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchingNews(searchQuery, searchNewsPage)
                searchNewsViewModel.postValue(handleSearchNews(response))
            } else {
                searchNewsViewModel.postValue(Resource.ErrorState("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNewsViewModel.postValue(Resource.ErrorState("Network Failure"))
                else -> searchNewsViewModel.postValue(Resource.ErrorState("Conversion Error"))
            }
        }
    }

    private suspend fun safeIntendedNews(newsDomain: String) {
        intendedNewsViewModel.postValue(Resource.LoadingState())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getIntendedNewsSource(newsDomain,intendedNewsPage)
                intendedNewsViewModel.postValue(handleIntendedNewsSource(response))
            }
        }catch (t:Throwable){
            when (t) {
                is IOException -> intendedNewsViewModel.postValue(Resource.ErrorState("Network Failure"))
                else -> intendedNewsViewModel.postValue(Resource.ErrorState("Conversion Error"))
            }
        }
    }

    private fun handleBreakingNews(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { responseResult ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = responseResult
                } else {
                    val olArticles = breakingNewsResponse?.articles
                    val newArticle = responseResult.articles
                    olArticles?.addAll(newArticle)
                }
                return Resource.SuccessState(breakingNewsResponse ?: responseResult)
            }
        }
        return Resource.ErrorState(response.message())
    }

    private fun handleSearchNews(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { responseResult ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = responseResult
                } else {
                    val olArticles = searchNewsResponse?.articles
                    val newArticle = responseResult.articles
                    olArticles?.addAll(newArticle)
                }
                return Resource.SuccessState(searchNewsResponse ?: responseResult)
            }
        }
        return Resource.ErrorState(response.message())
    }

    private fun handleIntendedNewsSource(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful){
            response.body()?.let {resultResponse->
                intendedNewsPage++
                if (intendedNewsResponse == null) {
                    intendedNewsResponse = resultResponse
                }else {
                    val oldArticle = intendedNewsResponse?.articles
                    val newArticle = resultResponse.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.SuccessState(intendedNewsResponse ?: resultResponse)
            }
        }
        return Resource.ErrorState(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedArticle() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article?) = viewModelScope.launch {
        if (article != null) {
            newsRepository.deleteArticle(article)
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }


}
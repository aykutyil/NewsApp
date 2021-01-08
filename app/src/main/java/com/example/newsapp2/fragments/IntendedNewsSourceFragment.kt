package com.example.newsapp2.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp2.R
import com.example.newsapp2.adapter.NewsAdapter
import com.example.newsapp2.ui.NewsActivity
import com.example.newsapp2.ui.NewsViewModel
import com.example.newsapp2.util.Constants
import com.example.newsapp2.util.Resource
import kotlinx.android.synthetic.main.fragment_intended_news.*
import kotlinx.android.synthetic.main.fragment_intended_news.paginationProgressBar

class IntendedNewsSourceFragment:Fragment(R.layout.fragment_intended_news) {

    lateinit var viewModel :NewsViewModel
    lateinit var intendedAdapter : NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as NewsActivity).viewModel
        viewModel.getIntendedNewsSource("yenicaggazetesi.com.tr")
        setupAdapter()

        intendedAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }

            findNavController().navigate(
                R.id.action_intendedNewsSourceFragment_to_articleFragment,
                bundle
            )
        }

        viewModel.intendedNewsViewModel.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.SuccessState -> {
                    hideProgressBar()
                    it.data?.let { newsResponse ->

                        intendedAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE +2
                        isLastPage = viewModel.intendedNewsPage == totalPages
                        if(isLastPage) {
                            rvintendedNews.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.ErrorState -> {
                    hideProgressBar()
                    it.message?.let {message->
                        Toast.makeText(activity,"An error occured $message", Toast.LENGTH_LONG).show()
                    }
                }

                is Resource.LoadingState -> {
                    showProgressBar()
                }
            }
        })



    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(){
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getIntendedNewsSource("yenicaggazetesi.com.tr")
                isScrolling = false
            }
        }
    }

    private fun setupAdapter() {
        intendedAdapter = NewsAdapter()
        rvintendedNews.apply {
            adapter = intendedAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@IntendedNewsSourceFragment.scrollListener)
        }
    }
}
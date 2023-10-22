package com.example.myapplication.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.myapplication.data.local.room.MessageDao
import kotlinx.coroutines.delay

class PagingSource (private val dao: MessageDao, private val chatId: String) : PagingSource<Int, Message>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Message> {
        val page = params.key ?: 0

        return try {
            val entities = dao.getPagedList(params.loadSize,
                page * params.loadSize,
                chatId)

            // simulate page loading
            if (page != 0) delay(1000)

            LoadResult.Page(
                data = entities,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (entities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Message>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
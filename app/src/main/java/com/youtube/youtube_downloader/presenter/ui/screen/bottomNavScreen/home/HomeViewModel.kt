package com.youtube.youtube_downloader.presenter.ui.screen.bottomNavScreen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.youtube.domain.model.VideoResponse
import com.youtube.domain.usecase.GetSearchVideoUseCase
import com.youtube.domain.utils.Resource
import com.youtube.youtube_downloader.presenter.ui.screen.mainActivity.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSearchVideoUseCase: GetSearchVideoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _query = MutableStateFlow("")

    private var job: Job? = null

    init {
        createQuery()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createQuery() {
        _query
            .debounce(500)
            .filter { query ->
                return@filter query.isNotEmpty()
            }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                job?.cancel()
                search(query)
            }
            .flowOn(Dispatchers.Default)
            .onEach { results ->
                if (results.isEmpty()) {
                    _uiState.value = UiState.Error("No results found.")
                } else {
                    _uiState.value = UiState.Success(results)
                }
            }
            .launchIn(viewModelScope)

    }

    fun searchQuery(query: String) {
        _query.value = query
    }

    private suspend fun search(query: String): Flow<List<String>> = flow {
        job?.cancel()
        delay(1000)
        job = viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            when (val result = getSearchVideoUseCase(query)) {
                is Resource.Success -> {
                    val gson = Gson()
                    val type = object : TypeToken<VideoResponse>() {}.type
                    val data = gson.fromJson<VideoResponse>(result.data.toString(), type)
                    _uiState.value = UiState.Success(data)
                    _loading.value = false
                }

                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message.toString())
                    _loading.value = false
                }

                else -> {}
            }
        }
    }

}
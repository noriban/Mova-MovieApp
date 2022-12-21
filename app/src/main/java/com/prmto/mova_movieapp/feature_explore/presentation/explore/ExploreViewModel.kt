package com.prmto.mova_movieapp.feature_explore.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.prmto.mova_movieapp.R
import com.prmto.mova_movieapp.core.data.dto.Genre
import com.prmto.mova_movieapp.core.data.models.enums.Category
import com.prmto.mova_movieapp.core.data.models.enums.isTv
import com.prmto.mova_movieapp.core.domain.repository.ConnectivityObserver
import com.prmto.mova_movieapp.core.domain.repository.isAvaliable
import com.prmto.mova_movieapp.core.presentation.util.UiText
import com.prmto.mova_movieapp.core.util.Constants.DEFAULT_LANGUAGE
import com.prmto.mova_movieapp.feature_explore.data.dto.SearchDto
import com.prmto.mova_movieapp.feature_explore.domain.use_case.ExploreUseCases
import com.prmto.mova_movieapp.feature_explore.presentation.event.ExploreBottomSheetEvent
import com.prmto.mova_movieapp.feature_explore.presentation.event.ExploreFragmentEvent
import com.prmto.mova_movieapp.feature_explore.presentation.event.ExploreUiEvent
import com.prmto.mova_movieapp.feature_explore.presentation.filter_bottom_sheet.state.FilterBottomState
import com.prmto.mova_movieapp.feature_home.domain.models.Movie
import com.prmto.mova_movieapp.feature_home.domain.models.TvSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreUseCases: ExploreUseCases
) : ViewModel() {


    private val _language = MutableStateFlow(DEFAULT_LANGUAGE)
    val language = _language.asStateFlow()

    private val _genreList = MutableStateFlow<List<Genre>>(emptyList())
    val genreList = _genreList.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> get() = _query

    private val _filterBottomSheetState = MutableStateFlow(FilterBottomState())
    val filterBottomSheetState = _filterBottomSheetState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ExploreUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _connectivityState = MutableStateFlow(ConnectivityObserver.Status.Avaliable)
    val connectivityState: StateFlow<ConnectivityObserver.Status> = _connectivityState.asStateFlow()

    var handler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.d(throwable.toString())
    }

    init {
        viewModelScope.launch(handler) {
            exploreUseCases.getLanguageIsoCodeUseCase().collectLatest { language ->
                _language.value = language
                getGenreListByCategoriesState()
            }
        }
    }

    fun multiSearch(query: String): Flow<PagingData<SearchDto>> {
        return if (query.isNotEmpty()) {
            exploreUseCases.multiSearchUseCase(query = query, language = language.value)
        } else {
            flow { PagingData.empty<SearchDto>() }
        }
    }

    fun discoverMovie(): Flow<PagingData<Movie>> {
        return exploreUseCases.discoverMovieUseCase(
            language = language.value,
            filterBottomState = filterBottomSheetState.value
        )
    }

    fun discoverTv(): Flow<PagingData<TvSeries>> {
        return exploreUseCases.discoverTvUseCase(
            language = language.value,
            filterBottomState = filterBottomSheetState.value
        )
    }


    fun onEventExploreFragment(event: ExploreFragmentEvent) {
        when (event) {
            is ExploreFragmentEvent.MultiSearch -> {
                _query.value = event.query
                if (query.value.isNotEmpty()) {
                    _filterBottomSheetState.update { it.copy(categoryState = Category.SEARCH) }
                } else {
                    _filterBottomSheetState.update { it.copy(categoryState = Category.MOVIE) }
                }
            }
            is ExploreFragmentEvent.RemoveQuery -> {
                _query.value = ""
            }
            is ExploreFragmentEvent.NavigateToDetailBottomSheet -> {
                viewModelScope.launch {
                    _eventFlow.emit(ExploreUiEvent.NavigateTo(event.directions))
                }
            }
            is ExploreFragmentEvent.UpdateConnectivityStatus -> {
                _connectivityState.value = event.connectivityStatus
                if (!event.connectivityStatus.isAvaliable()) {
                    viewModelScope.launch {
                        _eventFlow.emit(ExploreUiEvent.ShowSnackbar(UiText.StringResource(R.string.internet_error)))
                    }
                }
            }
        }
    }

    fun onEventBottomSheet(event: ExploreBottomSheetEvent) {
        when (event) {
            is ExploreBottomSheetEvent.UpdateCategory -> {
                _filterBottomSheetState.update { it.copy(categoryState = event.checkedCategory) }
                getGenreListByCategoriesState()
                resetSelectedGenreIdsState()
            }

            is ExploreBottomSheetEvent.UpdateGenreList -> {
                resetSelectedGenreIdsState()
                _filterBottomSheetState.update { it.copy(checkedGenreIdsState = event.checkedList) }
            }

            is ExploreBottomSheetEvent.UpdateSort -> {
                _filterBottomSheetState.update { it.copy(checkedSortState = event.checkedSort) }
            }

            is ExploreBottomSheetEvent.ResetFilterBottomState -> {
                _filterBottomSheetState.value = FilterBottomState()
            }

            is ExploreBottomSheetEvent.Apply -> {
                viewModelScope.launch { _eventFlow.emit(ExploreUiEvent.PopBackStack) }
            }
        }
    }

    private fun resetSelectedGenreIdsState() {
        _filterBottomSheetState.update { it.copy(checkedGenreIdsState = emptyList()) }
    }

    private fun getGenreListByCategoriesState() {
        viewModelScope.launch(handler) {
            try {
                if (_filterBottomSheetState.value.categoryState.isTv()) {
                    getTvGenreList()
                } else {
                    getMovieGenreList()
                }
            } catch (e: Exception) {
                _eventFlow.emit(ExploreUiEvent.ShowSnackbar(UiText.StringResource(R.string.internet_error)))
                Timber.e("Didn't download genreList $e")
            }
        }
    }

    private fun getMovieGenreList() {
        viewModelScope.launch(handler) {
            exploreUseCases.movieGenreListUseCase(language.value).collectLatest { genreList ->
                _genreList.value = genreList
            }
        }
    }

    private fun getTvGenreList() {
        viewModelScope.launch(handler) {
            exploreUseCases.tvGenreListUseCase(language.value).collectLatest { genreList ->
                _genreList.value = genreList
            }
        }
    }
}




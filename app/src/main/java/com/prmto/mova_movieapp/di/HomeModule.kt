package com.prmto.mova_movieapp.di

import com.prmto.mova_movieapp.core.domain.repository.DataStoreOperations
import com.prmto.mova_movieapp.core.domain.repository.RemoteRepository
import com.prmto.mova_movieapp.core.domain.use_case.GetLanguageIsoCodeUseCase
import com.prmto.mova_movieapp.core.domain.use_case.GetMovieGenreListUseCase
import com.prmto.mova_movieapp.core.domain.use_case.GetTvGenreListUseCase
import com.prmto.mova_movieapp.core.domain.use_case.UpdateLanguageIsoCodeUseCase
import com.prmto.mova_movieapp.feature_home.data.repository.HomeRepositoryImpl
import com.prmto.mova_movieapp.feature_home.data_source.remote.HomeApi
import com.prmto.mova_movieapp.feature_home.domain.repository.HomeRepository
import com.prmto.mova_movieapp.feature_home.domain.use_cases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        homeApi: HomeApi
    ): HomeRepository {
        return HomeRepositoryImpl(homeApi)
    }

    @Provides
    @Singleton
    fun provideHomeUseCases(
        homeRepository: HomeRepository,
        dataStoreOperations: DataStoreOperations,
        remoteRepository: RemoteRepository
    ): HomeUseCases {
        return HomeUseCases(
            getMovieGenreList = GetMovieGenreListUseCase(remoteRepository),
            getTvGenreList = GetTvGenreListUseCase(remoteRepository),
            getNowPlayingMoviesUseCase = GetNowPlayingMoviesUseCase(homeRepository),
            getLanguageIsoCodeUseCase = GetLanguageIsoCodeUseCase(dataStoreOperations),
            getPopularMoviesUseCase = GetPopularMoviesUseCase(homeRepository),
            getTopRatedMoviesUseCase = GetTopRatedMoviesUseCase(homeRepository),
            getPopularTvSeriesUseCase = GetPopularTvSeriesUseCase(homeRepository),
            getTopRatedTvSeriesUseCase = GetTopRatedTvSeriesUseCase(homeRepository),
            updateLanguageIsoCodeUseCase = UpdateLanguageIsoCodeUseCase(dataStoreOperations)
        )
    }
}
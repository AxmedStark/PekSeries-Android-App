package com.example.pekseries.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.example.pekseries.data.remote.TmdbApi
import com.example.pekseries.data.remote.TvMazeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(context))
            .build()
    }

    @Provides
    @Singleton
    fun provideTvMazeApi(okHttpClient: OkHttpClient): TvMazeApi {
        return Retrofit.Builder()
            .baseUrl("https://api.tvmaze.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TvMazeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTmdbApi(okHttpClient: OkHttpClient): TmdbApi {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}
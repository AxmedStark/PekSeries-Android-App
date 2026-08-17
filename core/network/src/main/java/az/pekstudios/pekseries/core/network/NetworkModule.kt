package az.pekstudios.pekseries.core.network

import android.content.Context
import az.pekstudios.pekseries.core.network.remote.TmdbApi
import az.pekstudios.pekseries.core.network.remote.TvMazeApi
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
            builder.addInterceptor(ChuckerInterceptor(context))
        }

        return builder.build()
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
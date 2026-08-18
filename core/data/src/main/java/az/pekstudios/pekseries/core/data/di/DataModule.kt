package az.pekstudios.pekseries.core.data.di

import az.pekstudios.pekseries.core.data.repository.ShowRepositoryImpl
import az.pekstudios.pekseries.core.data.repository.SubscriptionRepositoryImpl
import az.pekstudios.pekseries.core.domain.repository.ShowRepository
import az.pekstudios.pekseries.core.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindShowRepository(impl: ShowRepositoryImpl): ShowRepository

    @Binds
    @Singleton
    fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository
}

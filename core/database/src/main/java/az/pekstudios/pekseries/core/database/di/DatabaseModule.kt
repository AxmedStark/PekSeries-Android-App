package az.pekstudios.pekseries.core.database.di

import android.content.Context
import androidx.room.Room
import az.pekstudios.pekseries.core.database.PekDatabase
import az.pekstudios.pekseries.core.database.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePekDatabase(@ApplicationContext context: Context): PekDatabase {
        return Room.databaseBuilder(
            context,
            PekDatabase::class.java,
            "pekseries_db"
        ).build()
    }

    @Provides
    fun provideNotificationDao(database: PekDatabase): NotificationDao {
        return database.notificationDao()
    }
}
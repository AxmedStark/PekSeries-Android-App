package az.pekstudios.pekseries.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = true)
abstract class PekDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
package az.pekstudios.pekseries.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("DELETE FROM notification_history")
    suspend fun deleteAllNotifications(): Int
}
package az.pekstudios.pekseries.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val showId: String?,
    val timestamp: Long = System.currentTimeMillis()
)
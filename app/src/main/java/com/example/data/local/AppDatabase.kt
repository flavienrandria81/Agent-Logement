package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Agency
import com.example.data.model.Conversation
import com.example.data.model.ConversationParticipant
import com.example.data.model.Favorite
import com.example.data.model.Message
import com.example.data.model.Notification
import com.example.data.model.Property
import com.example.data.model.PropertyImage
import com.example.data.model.PropertyView
import com.example.data.model.Report
import com.example.data.model.User
import com.example.data.model.VisitRequest

@Database(
    entities = [
        User::class,
        Agency::class,
        Property::class,
        PropertyImage::class,
        Favorite::class,
        VisitRequest::class,
        Conversation::class,
        ConversationParticipant::class,
        Message::class,
        Notification::class,
        Report::class,
        PropertyView::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun propertyDao(): PropertyDao
    abstract fun interactionDao(): InteractionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "logement_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

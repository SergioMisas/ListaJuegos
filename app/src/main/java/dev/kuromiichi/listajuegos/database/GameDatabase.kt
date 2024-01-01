package dev.kuromiichi.listajuegos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.kuromiichi.listajuegos.models.Game
import dev.kuromiichi.listajuegos.models.Platform

@Database(entities = [Game::class, Platform::class], version = 1)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun platformDao(): PlatformDao

    companion object {
        private const val DATABASE_NAME = "game_database"
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        GameDatabase::class.java,
                        DATABASE_NAME
                    ).build()
            }
            return INSTANCE!!
        }
    }
}
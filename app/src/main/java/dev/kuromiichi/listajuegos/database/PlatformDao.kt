package dev.kuromiichi.listajuegos.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.kuromiichi.listajuegos.models.Platform

@Dao
interface PlatformDao {
    @Query("SELECT * FROM platform")
    fun findAll(): List<Platform>

    @Insert
    fun insert(platform: Platform)
}
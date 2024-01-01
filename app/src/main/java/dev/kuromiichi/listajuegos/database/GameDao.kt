package dev.kuromiichi.listajuegos.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.kuromiichi.listajuegos.models.Game

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    fun findAll(): List<Game>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(game: Game)

    @Update
    fun update(game: Game)

    @Delete
    fun delete(game: Game)
}
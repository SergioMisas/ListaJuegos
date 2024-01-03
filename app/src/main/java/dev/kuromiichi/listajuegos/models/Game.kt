package dev.kuromiichi.listajuegos.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Platform::class,
            parentColumns = ["name"],
            childColumns = ["platform"]
        )]
)
data class Game(
    @PrimaryKey val name: String,
    val image: String,
    val platform: String,
    val status: Status,
    val isFavorite: Boolean,
    val startDate: String,
    val finishDate: String
) {
    enum class Status(val text: String) {
        PLAYING("Jugando"),
        FINISHED("Terminado"),
        PENDING("Pendiente")
    }
}
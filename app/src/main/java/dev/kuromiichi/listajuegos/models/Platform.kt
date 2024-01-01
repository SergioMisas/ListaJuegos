package dev.kuromiichi.listajuegos.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Platform(
    @PrimaryKey val name: String
)
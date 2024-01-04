package dev.kuromiichi.listajuegos.listeners

import dev.kuromiichi.listajuegos.models.Game

interface GameOnClickListener {
    fun onClick(game: Game)
}
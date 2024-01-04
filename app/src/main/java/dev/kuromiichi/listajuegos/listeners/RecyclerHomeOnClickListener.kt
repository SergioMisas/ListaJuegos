package dev.kuromiichi.listajuegos.listeners

import dev.kuromiichi.listajuegos.models.Game

interface RecyclerHomeOnClickListener {
    fun onGameClick(game: Game)
}
package dev.kuromiichi.listajuegos.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.kuromiichi.listajuegos.R
import dev.kuromiichi.listajuegos.databinding.ItemGameBinding
import dev.kuromiichi.listajuegos.listeners.RecyclerHomeOnClickListener
import dev.kuromiichi.listajuegos.models.Game

class RecyclerHomeAdapter(
    val games: List<Game>,
    val listener: RecyclerHomeOnClickListener
) : RecyclerView.Adapter<RecyclerHomeAdapter.ViewHolder>() {

    inner class ViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {

        private val binding = ItemGameBinding.bind(view)

        fun bind(game: Game) {
            when (game.isFavorite) {
                true -> binding.imageViewFavorito.setImageResource(R.drawable.icon_favourite)
                false -> binding.imageViewFavorito.setImageResource(R.drawable.icon_favourite_border)
            }

            Glide.with(binding.imageViewJuego).load(game.image)
                .error(android.R.drawable.ic_menu_report_image).into(binding.imageViewJuego)
            binding.textViewTitulo.text = game.name
            binding.textViewPlataforma.text = game.platform

            when (game.status) {
                Game.Status.PENDING -> {
                    binding.textViewFechaInicio.visibility = ViewGroup.GONE
                    binding.textViewFechaFin.visibility = ViewGroup.GONE
                    binding.textViewEstado.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.textViewEstado.context,
                            R.color.red
                        )
                    )
                }

                Game.Status.PLAYING -> {
                    binding.textViewFechaInicio.visibility = ViewGroup.VISIBLE
                    binding.textViewFechaInicio.text = "Fecha de inicio: ${game.startDate}"
                    binding.textViewFechaFin.visibility = ViewGroup.GONE
                    binding.textViewEstado.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.textViewEstado.context,
                            R.color.blue
                        )
                    )
                }

                Game.Status.FINISHED -> {
                    binding.textViewFechaInicio.visibility = ViewGroup.VISIBLE
                    binding.textViewFechaInicio.text = "Fecha de inicio: ${game.startDate}"
                    binding.textViewFechaFin.visibility = ViewGroup.VISIBLE
                    binding.textViewFechaFin.text = "Fecha de finalizado: ${game.finishDate}"
                    binding.textViewEstado.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.textViewEstado.context,
                            R.color.green
                        )
                    )
                }
            }
        }

        fun setListeners(game: Game) {
            binding.imageViewJuego.setOnClickListener {
                listener.onGameClick(game)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_game,
            parent,
            false
        )
        return ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: RecyclerHomeAdapter.ViewHolder, position: Int) {
        holder.bind(games[position])
        holder.setListeners(games[position])
    }

    override fun getItemCount(): Int {
        return games.size
    }

}
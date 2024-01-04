package dev.kuromiichi.listajuegos.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.kuromiichi.listajuegos.adapters.RecyclerHomeAdapter
import dev.kuromiichi.listajuegos.database.GameDatabase
import dev.kuromiichi.listajuegos.databinding.DialogModifyBinding
import dev.kuromiichi.listajuegos.databinding.FragmentHomeBinding
import dev.kuromiichi.listajuegos.listeners.RecyclerHomeOnClickListener
import dev.kuromiichi.listajuegos.models.Game
import dev.kuromiichi.listajuegos.models.Platform
import dev.kuromiichi.listajuegos.validators.validate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), RecyclerHomeOnClickListener {
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mAdapter: RecyclerHomeAdapter
    private var games: List<Game> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        val gameDao = GameDatabase.getInstance(requireContext()).gameDao()
        Thread {
            games = gameDao.findAll()
        }.apply {
            start()
            join()
        }
        mAdapter = RecyclerHomeAdapter(games, this)
        mBinding.recyclerViewHome.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    override fun onGameClick(game: Game) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(game.name)
            setMessage(
                    "Nombre: ${game.name}\n" +
                    "Plataforma: ${game.platform}\n" +
                    "Status: ${game.status}\n" +
                    "Favorito: ${game.isFavorite}\n" +
                    "FechaInicio:${game.startDate}\n" +
                    "FechaFin:${game.finishDate}"
            )

            setPositiveButton("Fav/Unfav") { _, _ ->
                val gameNew = game.copy(isFavorite = !game.isFavorite)
                Thread {
                    GameDatabase.getInstance(requireContext()).gameDao().update(gameNew)
                }.apply {
                    start()
                    join()
                }
                setRecycler()

            }

            setNeutralButton("Modificar") { _, _ ->
                setModifyDialog(game)
            }
        }.show()
    }

    private fun setModifyDialog(game: Game) {
        AlertDialog.Builder(requireContext()).apply {
            val binding = DialogModifyBinding.inflate(layoutInflater)


            setView(binding.root)

            setTitle(game.name)

            val platforms = findPlatforms()
            binding.spinnerPlatform.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                platforms.map { it.name }
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            val statusItems = Game.Status.entries.map { it.text }
            binding.spinnerState.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusItems)
                    .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            Glide.with(requireContext()).load(game.image).into(binding.imageViewImage)
            binding.textInputImage.editText?.setText(game.image)
            binding.textInputDateStart.editText?.setText(game.startDate)
            binding.textInputDateEnd.editText?.setText(game.finishDate)

            binding.imageButtonDateStart.setOnClickListener {
                datePickerDialog(binding.textInputDateStart.editText!!)
            }

            binding.imageButtonDateEnd.setOnClickListener {
                datePickerDialog(binding.textInputDateEnd.editText!!)
            }

            binding.textInputImage.editText?.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(text.toString())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.imageViewImage)
                }
            }

            setPositiveButton("Modificar") { _, _ ->
                val gameNew = game.copy(
                    platform = binding.spinnerPlatform.selectedItem.toString(),
                    image = binding.textInputImage.editText?.text.toString(),
                    startDate = binding.textInputDateStart.editText?.text.toString(),
                    finishDate = binding.textInputDateEnd.editText?.text.toString()
                )

                val res = gameNew.validate()
                if (res != "") {
                    Toast.makeText(requireContext(), res, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                var success = true
                Thread {
                    try {
                        GameDatabase.getInstance(requireContext()).gameDao().update(gameNew)
                    } catch (e: SQLiteConstraintException) {
                        success = false
                    }
                }.apply {
                    start()
                    join()
                }
                if (!success) {
                    Toast.makeText(
                        requireContext(),
                        "Ya existe un juego con ese nombre",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                Toast.makeText(requireContext(), "Juego modificado", Toast.LENGTH_LONG).show()
                setRecycler()
            }

            setNeutralButton("Eliminar") { _, _ ->
                Thread {
                    GameDatabase.getInstance(requireContext()).gameDao().delete(game)
                }
                .apply {
                    start()
                    join()
                }
                setRecycler()
            }

            setNegativeButton("Cancelar") { _, _ -> }


        }.show()

    }

    private fun datePickerDialog(textInput: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                textInput.setText(
                    SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    ).format(calendar.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



    private fun findPlatforms(): List<Platform> {
        var platforms: List<Platform> = emptyList()
        Thread {
            platforms = GameDatabase.getInstance(requireContext()).platformDao().findAll()
        }.apply {
            start()
            join()
        }
        return platforms
    }
}
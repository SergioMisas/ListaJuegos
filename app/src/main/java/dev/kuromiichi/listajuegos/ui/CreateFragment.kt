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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.kuromiichi.listajuegos.database.GameDatabase
import dev.kuromiichi.listajuegos.databinding.DialogCreatePlatformBinding
import dev.kuromiichi.listajuegos.databinding.FragmentCreateBinding
import dev.kuromiichi.listajuegos.models.Game
import dev.kuromiichi.listajuegos.models.Platform
import dev.kuromiichi.listajuegos.validators.validate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateFragment : Fragment() {
    private lateinit var mBinding: FragmentCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCreateBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons()
        setSpinners()
        setImage()
        setCreateButton()
    }

    private fun setButtons() {
        mBinding.imageButtonDateStart.setOnClickListener {
            mBinding.textInputDateStart.editText?.let { datePickerDialog(it) }
        }

        mBinding.imageButtonDateEnd.setOnClickListener {
            mBinding.textInputDateEnd.editText?.let { datePickerDialog(it) }
        }

        mBinding.imageButtonAddPlatform.setOnClickListener {
            createPlatformDialog()
        }
    }

    private fun createPlatformDialog() {
        AlertDialog.Builder(requireContext()).apply {
            val binding = DialogCreatePlatformBinding.inflate(layoutInflater)
            setView(binding.root)

            setTitle("Añadir plataforma")

            setPositiveButton("Confirmar") { _, _ ->
                if (binding.editTextPlatform.text.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "El nombre no puede estar vacío",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                val platforms = findPlatforms()
                if (platforms.any { it.name == binding.editTextPlatform.text.toString() }) {
                    Toast.makeText(
                        requireContext(),
                        "La plataforma ya existe",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                Thread {
                    GameDatabase.getInstance(requireContext()).platformDao().insert(
                        Platform(binding.editTextPlatform.text.toString())
                    )
                }.apply {
                    start()
                    join()
                }
                Toast.makeText(
                    requireContext(),
                    "Plataforma añadida: ${binding.editTextPlatform.text}",
                    Toast.LENGTH_LONG
                ).show()

                setSpinners()
            }

            setNegativeButton("Cancelar") { _, _ -> }
        }.show()
    }

    private fun setSpinners() {
        val platforms = findPlatforms()
        mBinding.spinnerPlatform.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            platforms.map { it.name }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val statusItems = Game.Status.entries.map { it.text }
        mBinding.spinnerState.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusItems)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
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

    private fun setImage() {
        mBinding.textInputImage.editText?.doOnTextChanged { text, _, _, _ ->
            Glide.with(requireContext())
                .load(text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(android.R.drawable.ic_menu_report_image)
                .into(mBinding.imageViewImage)
        }
    }

    private fun setCreateButton() {
        mBinding.floatingActionButtonAddGame.setOnClickListener {
            if (mBinding.spinnerPlatform.selectedItemPosition == -1) {
                Toast.makeText(
                    requireContext(),
                    "No se ha seleccionado una plataforma",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val game = Game(
                name = mBinding.textInputName.editText?.text.toString(),
                image = mBinding.textInputImage.editText?.text.toString(),
                platform = mBinding.spinnerPlatform.selectedItem.toString(),
                status = Game.Status.entries[mBinding.spinnerState.selectedItemPosition],
                isFavorite = false,
                startDate = mBinding.textInputDateStart.editText?.text.toString(),
                finishDate = mBinding.textInputDateEnd.editText?.text.toString()
            )

            val res = game.validate()
            if (res != "") {
                Toast.makeText(requireContext(), res, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            var success = true
            Thread {
                try {
                    GameDatabase.getInstance(requireContext()).gameDao().insert(game)
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
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "Juego añadido", Toast.LENGTH_LONG).show()
        }
    }
}
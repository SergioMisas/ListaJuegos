package dev.kuromiichi.listajuegos.validators

import dev.kuromiichi.listajuegos.models.Game
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Game.validate(): String {
    if (name.isBlank())
        return "El nombre no puede estar vacío"
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    try {
        if (startDate.isNotBlank() && finishDate.isNotBlank()) {
            val dateStart = format.parse(startDate) ?: Date()
            val dateEnd = format.parse(finishDate) ?: Date()
            if (dateStart.after(dateEnd))
                return "La fecha de inicio no puede ser posterior a la fecha de finalización"
        }
    } catch (e: ParseException) {
        return "Las fechas no son válidas (vacías o en formato dd/MM/yyyy)"
    }
    if (platform.isBlank())
        return "La plataforma no puede estar vacía"
    return ""
}
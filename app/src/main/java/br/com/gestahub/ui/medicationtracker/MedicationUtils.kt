package br.com.gestahub.ui.medicationtracker

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

// Simplificado para usar apenas a descrição em texto
data class DoseInfo(
    val description: String,
    val originalIndex: Int
)

/**
 * Verifica se um medicamento está ativo em uma data específica.
 */
fun isMedicationActiveOnDate(med: Medication, targetDate: LocalDate): Boolean {
    val startDate = try {
        LocalDate.parse(med.startDate, DATE_FORMATTER)
    } catch (e: Exception) {
        return false
    }

    if (targetDate.isBefore(startDate)) {
        return false
    }

    return when (med.durationType) {
        "CONTINUOUS" -> true
        "DAYS" -> {
            val duration = med.durationValue ?: 0L
            val endDate = startDate.plusDays(duration)
            targetDate.isBefore(endDate)
        }
        else -> true
    }
}

/**
 * Gera a lista de doses para um medicamento em um dia específico (LÓGICA FINAL CORRIGIDA).
 */
fun getDosesForDay(med: Medication, targetDate: LocalDate): List<DoseInfo> {
    val startDate = try {
        LocalDate.parse(med.startDate, DATE_FORMATTER)
    } catch (e: Exception) {
        return emptyList()
    }

    return when (med.scheduleType) {
        // V CORREÇÃO AQUI: Agora lida com textos e horários V
        "FIXED_TIMES", "FLEXIBLE" -> {
            med.doses.mapIndexed { index, description ->
                DoseInfo(description, index)
            }
        }
        // ^ FIM DA CORREÇÃO ^
        "INTERVAL" -> {
            val interval = med.intervalHours ?: return emptyList()
            if (med.doses.isEmpty() || interval <= 0) return emptyList()

            val firstDoseTime = try {
                LocalTime.parse(med.doses.first())
            } catch (e: Exception) {
                return emptyList()
            }

            val startDateTime = LocalDateTime.of(startDate, firstDoseTime)
            var currentDoseDateTime = startDateTime
            var doseIndex = 0
            val dosesForTargetDay = mutableListOf<DoseInfo>()

            while (currentDoseDateTime.toLocalDate().isBefore(targetDate)) {
                currentDoseDateTime = currentDoseDateTime.plusHours(interval)
                doseIndex++
            }

            while (currentDoseDateTime.toLocalDate().isEqual(targetDate)) {
                dosesForTargetDay.add(
                    DoseInfo(
                        description = currentDoseDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        originalIndex = doseIndex
                    )
                )
                currentDoseDateTime = currentDoseDateTime.plusHours(interval)
                doseIndex++
            }

            dosesForTargetDay
        }
        else -> emptyList()
    }
}
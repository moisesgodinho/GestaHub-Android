package br.com.gestahub.ui.medicationtracker

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

data class DoseInfo(
    val time: LocalTime,
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
            // A data final é o primeiro dia em que o medicamento NÃO está mais ativo.
            val endDate = startDate.plusDays(duration)
            targetDate.isBefore(endDate)
        }
        else -> true
    }
}

/**
 * Gera a lista de doses para um medicamento em um dia específico (LÓGICA CORRIGIDA).
 */
fun getDosesForDay(med: Medication, targetDate: LocalDate): List<DoseInfo> {
    val startDate = try {
        LocalDate.parse(med.startDate, DATE_FORMATTER)
    } catch (e: Exception) {
        return emptyList()
    }

    return when (med.scheduleType) {
        "FIXED_TIMES", "FLEXIBLE" -> {
            med.doses.mapIndexedNotNull { index, timeString ->
                try {
                    DoseInfo(LocalTime.parse(timeString), index)
                } catch (e: Exception) {
                    null
                }
            }
        }
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

            // Avança no tempo até encontrar a primeira dose no dia alvo ou depois
            while (currentDoseDateTime.toLocalDate().isBefore(targetDate)) {
                currentDoseDateTime = currentDoseDateTime.plusHours(interval)
                doseIndex++
            }

            // Agora que estamos no dia certo (ou depois), coleta todas as doses que caem nesse dia
            while (currentDoseDateTime.toLocalDate().isEqual(targetDate)) {
                dosesForTargetDay.add(DoseInfo(currentDoseDateTime.toLocalTime(), doseIndex))
                currentDoseDateTime = currentDoseDateTime.plusHours(interval)
                doseIndex++
            }

            dosesForTargetDay
        }
        else -> emptyList()
    }
}
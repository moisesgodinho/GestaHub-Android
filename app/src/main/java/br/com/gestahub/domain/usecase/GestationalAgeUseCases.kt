package br.com.gestahub.domain.usecase

import br.com.gestahub.data.WeeklyInfoProvider
import br.com.gestahub.domain.model.GestationalInfo
import br.com.gestahub.ui.home.GestationalData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

// Classe de dados para guardar o resultado do cálculo
data class GestationalAge(val weeks: Long, val days: Long)

class CalculateGestationalInfoUseCase {

    // A lógica para obter a DUM estimada agora é uma função privada aqui dentro,
    // o que resolve o erro de referência e melhora o encapsulamento.
    private fun getEstimatedLmp(data: GestationalData): LocalDate? {
        val lmpDate = data.lmp?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val ultrasoundDate = data.ultrasoundExamDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val weeks = data.weeksAtExam?.toIntOrNull() ?: 0
        val days = data.daysAtExam?.toIntOrNull() ?: 0

        if (ultrasoundDate != null && (weeks > 0 || days > 0)) {
            val daysAtExamTotal = (weeks * 7) + days
            return ultrasoundDate.minusDays(daysAtExamTotal.toLong())
        }
        return lmpDate
    }

    operator fun invoke(data: GestationalData): GestationalInfo? {
        val estimatedLmp = getEstimatedLmp(data) ?: return null

        val today = LocalDate.now()
        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()
        val currentWeeks = gestationalAgeInDays / 7
        val currentDays = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        val weekForInfo = if (currentWeeks < 1) 1 else if (currentWeeks > 42) 42 else currentWeeks
        val weeklyInfo = WeeklyInfoProvider.getInfoForWeek(weekForInfo)

        return GestationalInfo(
            gestationalWeeks = currentWeeks,
            gestationalDays = currentDays,
            dueDate = dueDate.format(dateFormatter),
            countdownWeeks = if (remainingDays >= 0) remainingDays / 7 else 0,
            countdownDays = if (remainingDays >= 0) remainingDays % 7 else 0,
            weeklyInfo = weeklyInfo,
            estimatedLmp = estimatedLmp
        )
    }
}

// Esta classe permanece para ser usada pelos ViewModels, que passam o perfil como um Map.
class GetEstimatedLmpUseCase {
    operator fun invoke(gestationalProfile: Map<*, *>?): LocalDate? {
        if (gestationalProfile == null) return null

        val lmpString = gestationalProfile["lmp"] as? String
        val ultrasoundMap = gestationalProfile["ultrasound"] as? Map<*, *>

        if (ultrasoundMap != null) {
            val examDateString = ultrasoundMap["examDate"] as? String
            val weeksAtExam = (ultrasoundMap["weeksAtExam"] as? String)?.toLongOrNull()
            val daysAtExam = (ultrasoundMap["daysAtExam"] as? String)?.toLongOrNull()

            if (examDateString != null && weeksAtExam != null && daysAtExam != null) {
                val examDate = runCatching { LocalDate.parse(examDateString) }.getOrNull()
                if (examDate != null) {
                    val totalDaysAtExam = (weeksAtExam * 7) + daysAtExam
                    return examDate.minusDays(totalDaysAtExam)
                }
            }
        }

        return lmpString?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }
}

class CalculateGestationalAgeOnDateUseCase {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale("pt", "BR"))

    fun forUI(lmp: LocalDate, startWeek: Int): String {
        val startDate = lmp.plusWeeks((startWeek - 1).toLong())
        return startDate.format(displayFormatter)
    }

    fun forUI(lmp: LocalDate, endWeek: Int, isEnd: Boolean): String {
        val endDate = lmp.plusWeeks(endWeek.toLong()).minusDays(1)
        return endDate.format(displayFormatter)
    }

    operator fun invoke(lmp: LocalDate, currentDate: LocalDate): GestationalAge {
        val totalDays = ChronoUnit.DAYS.between(lmp, currentDate)
        if (totalDays < 0) return GestationalAge(0, 0)
        val weeks = totalDays / 7
        val days = totalDays % 7
        return GestationalAge(weeks, days)
    }
}
package br.com.gestahub.domain.usecase

import br.com.gestahub.data.WeeklyInfo
import br.com.gestahub.data.WeeklyInfoProvider
import br.com.gestahub.domain.model.GestationalInfo
import br.com.gestahub.ui.home.GestationalData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class CalculateGestationalInfoUseCase {

    operator fun invoke(data: GestationalData): GestationalInfo? {
        val lmpDate = data.lmp?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val ultrasoundDate = data.ultrasoundExamDate?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }
        val weeks = data.weeksAtExam?.toIntOrNull() ?: 0
        val days = data.daysAtExam?.toIntOrNull() ?: 0

        val estimatedLmp = getEstimatedLmp(lmpDate, ultrasoundDate, weeks, days) ?: return null

        val today = LocalDate.now()
        val gestationalAgeInDays = ChronoUnit.DAYS.between(estimatedLmp, today).toInt()
        val currentWeeks = gestationalAgeInDays / 7
        val currentDays = gestationalAgeInDays % 7

        val dueDate = estimatedLmp.plusDays(280)
        val remainingDays = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        val weekForInfo = if (currentWeeks < 1) 1 else if (currentWeeks > 42) 42 else currentWeeks + 1
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

    private fun getEstimatedLmp(lmp: LocalDate?, ultrasoundExamDate: LocalDate?, weeksAtExam: Int, daysAtExam: Int): LocalDate? {
        if (ultrasoundExamDate != null && (weeksAtExam > 0 || daysAtExam > 0)) {
            val daysAtExamTotal = (weeksAtExam * 7) + daysAtExam
            return ultrasoundExamDate.minusDays(daysAtExamTotal.toLong())
        }
        return lmp
    }
}
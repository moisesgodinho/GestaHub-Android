package br.com.gestahub.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Classe de dados para guardar o resultado do cálculo
data class GestationalAge(val weeks: Long, val days: Long)

object GestationalAgeCalculator {

    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale("pt", "BR"))

    // --- FUNÇÃO ADICIONADA ---
    /**
     * Calcula a idade gestacional em semanas e dias.
     * @param lmp A data da última menstruação.
     * @param currentDate A data para a qual a idade gestacional deve ser calculada.
     * @return Um objeto GestationalAge contendo semanas e dias.
     */
    fun calculateGestationalAge(lmp: LocalDate, currentDate: LocalDate): GestationalAge {
        val totalDays = ChronoUnit.DAYS.between(lmp, currentDate)
        if (totalDays < 0) return GestationalAge(0, 0)
        val weeks = totalDays / 7
        val days = totalDays % 7
        return GestationalAge(weeks, days)
    }

    fun getEstimatedLmp(gestationalProfile: Map<*, *>?): LocalDate? {
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

    /**
     * Calcula a data de início da janela ideal para um ultrassom.
     * @param lmp A data da última menstruação.
     * @param startWeek A semana gestacional em que a janela começa (ex: 8).
     * @return A data de início formatada como "dd/MM/yy".
     */
    fun getWindowStartDate(lmp: LocalDate, startWeek: Int): String {
        val startDate = lmp.plusWeeks((startWeek - 1).toLong())
        return startDate.format(displayFormatter)
    }

    /**
     * Calcula a data de fim da janela ideal para um ultrassom.
     * @param lmp A data da última menstruação.
     * @param endWeek A semana gestacional em que a janela termina (ex: 11).
     * @return A data de fim formatada como "dd/MM/yy".
     */
    fun getWindowEndDate(lmp: LocalDate, endWeek: Int): String {
        val endDate = lmp.plusWeeks(endWeek.toLong()).minusDays(1)
        return endDate.format(displayFormatter)
    }
}
// Local: app/src/main/java/br/com/gestahub/util/GestationalAgeCalculator.kt
package br.com.gestahub.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object GestationalAgeCalculator {

    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale("pt", "BR"))

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
        // --- CORREÇÃO APLICADA AQUI ---
        // O início da semana N é após (N-1) semanas completas da DUM.
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
        // --- CORREÇÃO APLICADA AQUI ---
        // O fim da semana N é ao final de N semanas completas, menos um dia.
        // Ex: O fim da semana 11 é DUM + 11 semanas - 1 dia.
        val endDate = lmp.plusWeeks(endWeek.toLong()).minusDays(1)
        return endDate.format(displayFormatter)
    }
}
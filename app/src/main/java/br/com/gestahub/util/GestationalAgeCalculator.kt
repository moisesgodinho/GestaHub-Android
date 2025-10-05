// Local: app/src/main/java/br/com/gestahub/util/GestationalAgeCalculator.kt
package br.com.gestahub.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object GestationalAgeCalculator {

    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yy", Locale("pt", "BR"))

    /**
     * Calcula a DUM (Data da Última Menstruação) estimada, priorizando os dados do ultrassom.
     * Esta é a função central para obter a data mais precisa para o início da gestação.
     * @param gestationalProfile Um mapa contendo os dados do perfil gestacional do usuário.
     * @return A DUM estimada como um objeto LocalDate, ou null se não houver dados suficientes.
     */
    fun getEstimatedLmp(gestationalProfile: Map<*, *>?): LocalDate? {
        if (gestationalProfile == null) return null

        val lmpString = gestationalProfile["lmp"] as? String
        val ultrasoundMap = gestationalProfile["ultrasound"] as? Map<*, *>

        // Tenta calcular a DUM a partir do ultrassom primeiro
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

        // Se não houver dados de ultrassom, usa a DUM informada
        return lmpString?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }

    fun getWindowStartDate(lmp: LocalDate, startWeek: Int): String {
        val startDate = lmp.plusDays((startWeek * 7).toLong())
        return startDate.format(displayFormatter)
    }

    fun getWindowEndDate(lmp: LocalDate, endWeek: Int): String {
        val endDate = lmp.plusDays((endWeek * 7 + 6).toLong())
        return endDate.format(displayFormatter)
    }
}
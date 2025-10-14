package br.com.gestahub.domain.model

import br.com.gestahub.data.WeeklyInfo
import java.time.LocalDate

data class GestationalInfo(
    val gestationalWeeks: Int,
    val gestationalDays: Int,
    val dueDate: String,
    val countdownWeeks: Int,
    val countdownDays: Int,
    val weeklyInfo: WeeklyInfo?,
    val estimatedLmp: LocalDate
)
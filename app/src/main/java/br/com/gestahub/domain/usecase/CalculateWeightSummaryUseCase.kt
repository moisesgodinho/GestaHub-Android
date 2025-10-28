package br.com.gestahub.domain.usecase

import br.com.gestahub.ui.weight.WeightEntry
import br.com.gestahub.ui.weight.WeightProfile
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

// Data class para encapsular o resultado do cálculo
data class WeightSummary(
    val initialBmi: Double = 0.0,
    val currentBmi: Double = 0.0,
    val totalGain: Double = 0.0,
    val gainGoal: String = ""
)

class CalculateWeightSummaryUseCase @Inject constructor() {
    operator fun invoke(profile: WeightProfile?, entries: List<WeightEntry>): WeightSummary {
        if (profile == null || profile.height <= 0 || profile.prePregnancyWeight <= 0) {
            return WeightSummary()
        }

        val heightInMeters = profile.height / 100.0
        val initialWeight = profile.prePregnancyWeight
        val latestWeight = entries.firstOrNull()?.weight ?: initialWeight

        val unroundedInitialBmi = initialWeight / heightInMeters.pow(2)
        val initialBmi = (unroundedInitialBmi * 10).roundToInt() / 10.0

        val currentBmi = latestWeight / heightInMeters.pow(2)
        val totalGain = latestWeight - initialWeight

        val gainGoal = when {
            initialBmi < 18.5 -> "12.5 - 18.0 kg"
            initialBmi >= 18.5 && initialBmi < 25.0 -> "11.5 - 16.0 kg"
            initialBmi >= 25.0 && initialBmi < 30.0 -> "7.0 - 11.5 kg"
            initialBmi >= 30.0 -> "5.0 - 9.0 kg"
            else -> ""
        }

        return WeightSummary(
            initialBmi = initialBmi,
            currentBmi = currentBmi,
            totalGain = totalGain,
            gainGoal = gainGoal
        )
    }
}
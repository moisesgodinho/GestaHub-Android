package br.com.gestahub.domain.usecase

import br.com.gestahub.ui.weight.SimpleChartEntry
import br.com.gestahub.ui.weight.WeightEntry
import br.com.gestahub.ui.weight.WeightProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Data class para encapsular os dados formatados para o gráfico
data class WeightChartData(
    val entries: List<SimpleChartEntry> = emptyList(),
    val labels: List<String> = emptyList()
)

class PrepareWeightChartDataUseCase @Inject constructor() {
    operator fun invoke(
        entries: List<WeightEntry>,
        profile: WeightProfile?,
        estimatedLmp: LocalDate?
    ): WeightChartData {
        if (estimatedLmp == null || profile == null || profile.prePregnancyWeight <= 0) {
            return WeightChartData()
        }

        val chartEntries = mutableListOf<SimpleChartEntry>()
        val dateLabels = mutableListOf<String>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

        // Ponto inicial do gráfico com o peso pré-gestacional
        chartEntries.add(SimpleChartEntry(0f, profile.prePregnancyWeight.toFloat()))
        dateLabels.add("Início")

        // Adiciona cada registro de peso
        entries.sortedBy { it.date }.forEachIndexed { index, entry ->
            chartEntries.add(SimpleChartEntry(index + 1f, entry.weight.toFloat()))
            try {
                dateLabels.add(LocalDate.parse(entry.date).format(dateFormatter))
            } catch (e: Exception) {
                dateLabels.add("-")
            }
        }

        // Adiciona um ponto final representando a Data Provável do Parto (DPP)
        val dueDate = estimatedLmp.plusDays(280)
        if (dueDate.isAfter(LocalDate.now())) {
            val lastWeight = entries.firstOrNull()?.weight?.toFloat() ?: profile.prePregnancyWeight.toFloat()
            chartEntries.add(SimpleChartEntry(chartEntries.size.toFloat(), lastWeight))
            dateLabels.add("DPP")
        }

        return WeightChartData(entries = chartEntries, labels = dateLabels)
    }
}
package br.com.gestahub.ui.weight

// A anotação @DocumentId foi removida, pois agora usaremos a data como ID manual.
data class WeightEntry(
    val date: String = "",
    val weight: Double = 0.0,
)

/**
 * Representa o perfil de peso do usuário, contendo dados que não mudam com frequência.
 */
data class WeightProfile(
    val height: Int = 0,
    val prePregnancyWeight: Double = 0.0
)

/**
 * Classe de dados simples para armazenar os pontos do gráfico, removendo a dependência da Vico.
 */
data class SimpleChartEntry(val x: Float, val y: Float)
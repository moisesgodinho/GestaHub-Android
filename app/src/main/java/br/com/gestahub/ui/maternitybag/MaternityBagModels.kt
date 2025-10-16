package br.com.gestahub.ui.maternitybag

import com.google.firebase.firestore.PropertyName

// Representa um único item na lista da mala
data class MaternityBagItem(
    val id: String = "",
    val label: String = "",
    @get:PropertyName("isCustom") // Garante a serialização correta para o Firestore
    @set:PropertyName("isCustom")
    var isCustom: Boolean = false
)

// Representa uma categoria da mala (ex: "Para a Mamãe")
data class MaternityBagCategory(
    val title: String = "",
    val items: List<MaternityBagItem> = emptyList()
)

// Estrutura completa da mala maternidade
data class MaternityBagList(
    val mom: MaternityBagCategory = MaternityBagCategory(),
    val baby: MaternityBagCategory = MaternityBagCategory(),
    val companion: MaternityBagCategory = MaternityBagCategory(),
    val docs: MaternityBagCategory = MaternityBagCategory()
)

// Usado para armazenar os dados no perfil gestacional do usuário no Firestore
data class MaternityBagFirestore(
    val maternityBagList: MaternityBagList = MaternityBagList(),
    val maternityBagChecked: List<String> = emptyList()
)
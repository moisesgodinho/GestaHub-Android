package br.com.gestahub.data

import br.com.gestahub.ui.shoppinglist.ShoppingListCategory
import br.com.gestahub.ui.shoppinglist.ShoppingListItem

fun getDefaultShoppingList(): Map<String, ShoppingListCategory> {
    return mapOf(
        "quarto" to ShoppingListCategory(
            title = "Quarto do Bebê",
            items = listOf(
                ShoppingListItem(id = "q-01", label = "Berço"),
                ShoppingListItem(id = "q-02", label = "Colchão para berço"),
                ShoppingListItem(id = "q-03", label = "Cômoda / Trocador"),
                ShoppingListItem(id = "q-04", label = "Poltrona de amamentação"),
                ShoppingListItem(id = "q-05", label = "Kit berço (protetores e lençóis)"),
                ShoppingListItem(id = "q-06", label = "Móbile para berço"),
                ShoppingListItem(id = "q-07", label = "Cesto para roupas sujas"),
                ShoppingListItem(id = "q-08", label = "Babá eletrônica")
            )
        ),
        "roupas" to ShoppingListCategory(
            title = "Roupas",
            items = listOf(
                ShoppingListItem(id = "r-01", label = "Bodies de manga curta (RN e P)"),
                ShoppingListItem(id = "r-02", label = "Bodies de manga longa (RN e P)"),
                ShoppingListItem(id = "r-03", label = "Mijões / Culotes (RN e P)"),
                ShoppingListItem(id = "r-04", label = "Macacões (RN e P)"),
                ShoppingListItem(id = "r-05", label = "Meias"),
                ShoppingListItem(id = "r-06", label = "Luvinhas e toucas"),
                ShoppingListItem(id = "r-07", label = "Casaquinhos"),
                ShoppingListItem(id = "r-08", label = "Saída da maternidade")
            )
        ),
        "higiene" to ShoppingListCategory(
            title = "Higiene e Cuidados",
            items = listOf(
                ShoppingListItem(id = "h-01", label = "Fraldas descartáveis (RN e P)"),
                ShoppingListItem(id = "h-02", label = "Lenços umedecidos"),
                ShoppingListItem(id = "h-03", label = "Pomada para assaduras"),
                ShoppingListItem(id = "h-04", label = "Sabonete líquido (cabeça aos pés)"),
                ShoppingListItem(id = "h-05", label = "Banheira com suporte"),
                ShoppingListItem(id = "h-06", label = "Toalhas de banho com capuz"),
                ShoppingListItem(id = "h-07", label = "Kit de higiene (tesoura, escova, etc.)"),
                ShoppingListItem(id = "h-08", label = "Algodão e Cotonetes")
            )
        ),
        "passeio" to ShoppingListCategory(
            title = "Passeio",
            items = listOf(
                ShoppingListItem(id = "p-01", label = "Bebê conforto (obrigatório para carro)"),
                ShoppingListItem(id = "p-02", label = "Carrinho de bebê"),
                ShoppingListItem(id = "p-03", label = "Bolsa de maternidade / passeio"),
                ShoppingListItem(id = "p-04", label = "Trocador portátil"),
                ShoppingListItem(id = "p-05", label = "Sling ou canguru")
            )
        )
    )
}
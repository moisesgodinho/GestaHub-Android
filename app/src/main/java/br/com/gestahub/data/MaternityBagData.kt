package br.com.gestahub.data

import br.com.gestahub.ui.maternitybag.MaternityBagCategory
import br.com.gestahub.ui.maternitybag.MaternityBagItem
import br.com.gestahub.ui.maternitybag.MaternityBagList

object MaternityBagData {
    val defaultData = MaternityBagList(
        mom = MaternityBagCategory(
            title = "Para a Mamãe",
            items = listOf(
                MaternityBagItem(id = "mom-01", label = "Pijamas ou camisolas com abertura frontal"),
                MaternityBagItem(id = "mom-02", label = "Sutiãs de amamentação"),
                MaternityBagItem(id = "mom-03", label = "Calcinhas confortáveis de cintura alta"),
                MaternityBagItem(id = "mom-04", label = "Absorventes pós-parto"),
                MaternityBagItem(id = "mom-05", label = "Roupão ou hobby"),
                MaternityBagItem(id = "mom-06", label = "Chinelos ou pantufas"),
                MaternityBagItem(id = "mom-07", label = "Meias"),
                MaternityBagItem(id = "mom-08", label = "Roupa para a saída da maternidade"),
                MaternityBagItem(id = "mom-09", label = "Itens de higiene pessoal (escova, pasta, etc.)"),
                MaternityBagItem(id = "mom-10", label = "Carregador de celular"),
                MaternityBagItem(id = "mom-11", label = "Almofada de amamentação")
            )
        ),
        baby = MaternityBagCategory(
            title = "Para o Bebê",
            items = listOf(
                MaternityBagItem(id = "baby-01", label = "Macacões (tamanho RN e P)"),
                MaternityBagItem(id = "baby-02", label = "Bodies de manga curta e longa"),
                MaternityBagItem(id = "baby-03", label = "Calças (mijão/culote)"),
                MaternityBagItem(id = "baby-04", label = "Meias e luvinhas"),
                MaternityBagItem(id = "baby-05", label = "Toucas"),
                MaternityBagItem(id = "baby-06", label = "Manta ou cobertor"),
                MaternityBagItem(id = "baby-07", label = "Fraldas de boca"),
                MaternityBagItem(id = "baby-08", label = "Toalha de banho com capuz"),
                MaternityBagItem(id = "baby-09", label = "Fraldas descartáveis (tamanho RN)"),
                MaternityBagItem(id = "baby-10", label = "Sabonete líquido neutro (cabeça aos pés)"),
                MaternityBagItem(id = "baby-11", label = "Pomada para assaduras"),
                MaternityBagItem(id = "baby-12", label = "Escova de cabelo macia"),
                MaternityBagItem(id = "baby-13", label = "Saída da maternidade")
            )
        ),
        companion = MaternityBagCategory(
            title = "Para o Acompanhante",
            items = listOf(
                MaternityBagItem(id = "comp-01", label = "Troca de roupa confortável"),
                MaternityBagItem(id = "comp-02", label = "Itens de higiene pessoal"),
                MaternityBagItem(id = "comp-03", label = "Carregador de celular"),
                MaternityBagItem(id = "comp-04", label = "Lanches e garrafa de água"),
                MaternityBagItem(id = "comp-05", label = "Câmera ou celular para fotos")
            )
        ),
        docs = MaternityBagCategory(
            title = "Documentos",
            items = listOf(
                MaternityBagItem(id = "docs-01", label = "Documentos de identidade (RG, CNH)"),
                MaternityBagItem(id = "docs-02", label = "Cartão do plano de saúde"),
                MaternityBagItem(id = "docs-03", label = "Cartão de pré-natal / da gestante"),
                MaternityBagItem(id = "docs-04", label = "Últimos exames e ultrassons"),
                MaternityBagItem(id = "docs-05", label = "Plano de parto (se tiver)")
            )
        )
    )
}
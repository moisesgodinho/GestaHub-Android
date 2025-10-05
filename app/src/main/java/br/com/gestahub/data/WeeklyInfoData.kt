package br.com.gestahub.data

data class WeeklyInfo(
    val week: Int,
    val title: String,
    val size: String,
    val length: String,
    val weight: String,
    val baby: String,
    val mom: String
)

object WeeklyInfoProvider {
    val weeklyInfoList = listOf(
        WeeklyInfo(
            week = 1,
            title = "Semana 1",
            size = "Início da Jornada",
            length = "N/A",
            weight = "N/A",
            baby = "A contagem da gravidez começa no primeiro dia da sua última menstruação. Neste momento, a concepção ainda não ocorreu. Seu corpo está se preparando para uma possível gravidez.",
            mom = "Este é o início do seu ciclo menstrual. É um período ideal para focar em hábitos saudáveis, como tomar ácido fólico e manter uma dieta equilibrada."
        ),
        WeeklyInfo(
            week = 2,
            title = "Semana 2",
            size = "Período Fértil",
            length = "N/A",
            weight = "N/A",
            baby = "Seu corpo libera um óvulo (ovulação). Se um espermatozoide o encontrar, a fecundação acontecerá. É o momento mágico onde toda a informação genética do seu bebê é definida.",
            mom = "Você está no seu período mais fértil. Fique atenta aos sinais de ovulação, como um leve aumento da temperatura corporal basal ou alterações no muco cervical."
        ),
        WeeklyInfo(
            week = 3,
            title = "Semana 3",
            size = "Grão de Açúcar",
            length = "N/A",
            weight = "N/A",
            baby = "O óvulo fertilizado (zigoto) viaja em direção ao útero, dividindo-se rapidamente em um aglomerado de células chamado blastocisto. Ele já carrega todo o DNA do seu bebê.",
            mom = "Você ainda não sabe que está grávida. Enquanto isso, uma incrível jornada de desenvolvimento já começou dentro de você."
        ),
        WeeklyInfo(
            week = 4,
            title = "Semana 4",
            size = "Semente de Papoula",
            length = "0,1 - 0,2 cm",
            weight = "Menos de 1 g",
            baby = "O blastocisto se implanta na parede do útero (nidação). Esta etapa crucial dá início ao desenvolvimento do embrião e da placenta, que irá nutrir o bebê.",
            mom = "É nesta semana que sua menstruação atrasaria. Um teste de gravidez já pode dar positivo! Você pode sentir cólicas leves e notar um pequeno sangramento (de nidação)."
        ),
        WeeklyInfo(
            week = 5,
            title = "Semana 5",
            size = "Semente de Laranja",
            length = "0,2 - 0,4 cm",
            weight = "Menos de 1 g",
            baby = "O coração do embrião começa a se formar e a bater. O tubo neural, que se tornará o cérebro e a medula espinhal, também está se desenvolvendo.",
            mom = "Os sintomas podem começar a aparecer: náuseas, cansaço extremo e seios sensíveis. As mudanças hormonais são intensas. É hora de marcar sua primeira consulta pré-natal."
        ),
        WeeklyInfo(
            week = 6,
            title = "Semana 6",
            size = "Lentilha",
            length = "0,5 - 0,8 cm",
            weight = "Cerca de 1 g",
            baby = "Traços faciais como olhos e narinas começam a se delinear. Pequenos brotos que se tornarão braços e pernas são visíveis. O coração bate entre 100 e 160 vezes por minuto.",
            mom = "As náuseas matinais podem se intensificar. Tente fazer pequenas refeições ao longo do dia e mantenha-se hidratada. Seu olfato pode ficar muito mais apurado."
        ),
        WeeklyInfo(
            week = 7,
            title = "Semana 7",
            size = "Mirtilo",
            length = "1,2 - 1,5 cm",
            weight = "1 - 2 g",
            baby = "O cérebro está crescendo rapidamente, e as mãos e pés estão se formando, parecendo pequenas nadadeiras. Os principais órgãos, como pulmões e rins, continuam seu desenvolvimento.",
            mom = "Seu útero dobrou de tamanho. O cansaço pode ser avassalador, pois seu corpo está trabalhando arduamente para construir o sistema de suporte do bebê."
        ),
        WeeklyInfo(
            week = 8,
            title = "Semana 8",
            size = "Framboesa",
            length = "1,5 - 2 cm",
            weight = "2 - 3 g",
            baby = "Os dedos das mãos e dos pés começam a se formar. Pálpebras e a ponta do nariz já são visíveis. O bebê está constantemente se movendo, embora você ainda não consiga sentir.",
            mom = "Seus seios estão maiores e mais pesados. Desejos ou aversões a certos alimentos podem aparecer. Use um sutiã de bom suporte para maior conforto."
        ),
        WeeklyInfo(
            week = 9,
            title = "Semana 9",
            size = "Azeitona",
            length = "2,2 - 2,8 cm",
            weight = "3 - 5 g",
            baby = "As estruturas essenciais estão no lugar e a cauda embrionária desapareceu. As articulações (cotovelos, joelhos) já se dobram e os órgãos vitais estão cada vez mais complexos.",
            mom = "Suas emoções podem estar à flor da pele devido às alterações hormonais. É normal sentir-se eufórica em um momento e chorosa no outro."
        ),
        WeeklyInfo(
            week = 10,
            title = "Semana 10",
            size = "Morango",
            length = "3 - 4 cm",
            weight = "5 - 7 g",
            baby = "Seu bebê agora é oficialmente um feto! As unhas e impressões digitais começam a se formar. Órgãos vitais, como rins e fígado, estão funcionando e continuarão a amadurecer.",
            mom = "Sua barriguinha pode começar a aparecer. A circulação sanguínea aumentou, e você pode notar veias mais visíveis na pele, principalmente nos seios e pernas."
        ),
        WeeklyInfo(
            week = 11,
            title = "Semana 11",
            size = "Figo",
            length = "4 - 5 cm",
            weight = "7 - 10 g",
            baby = "O feto está quase todo formado. Ele pode abrir e fechar os punhos e os brotos dos dentes de leite aparecem sob a gengiva. O corpo começará a crescer mais rápido para equilibrar as proporções.",
            mom = "Os enjoos e o cansaço tendem a diminuir. Sua energia pode começar a voltar! É um bom momento para iniciar exercícios leves aprovados pelo seu médico."
        ),
        WeeklyInfo(
            week = 12,
            title = "Semana 12",
            size = "Limão",
            length = "5,5 - 6,5 cm",
            weight = "14 - 20 g",
            baby = "Os reflexos do bebê estão se desenvolvendo. Ele pode franzir a testa, fazer caretas e sugar o dedo. Os intestinos, que cresciam fora do corpo, agora estão totalmente dentro do abdômen.",
            mom = "O risco de aborto espontâneo diminui significativamente. Muitas pessoas se sentem mais seguras para compartilhar a notícia da gravidez agora."
        ),
        WeeklyInfo(
            week = 13,
            title = "Semana 13",
            size = "Pêssego",
            length = "7 - 8 cm",
            weight = "25 - 35 g",
            baby = "Fim do primeiro trimestre! As cordas vocais do seu bebê estão se formando. O corpo está crescendo mais rápido que a cabeça, tornando-o mais proporcional.",
            mom = "Bem-vinda ao segundo trimestre, a 'lua de mel' da gravidez! Você provavelmente se sentirá mais disposta e com menos sintomas. Sua barriga está cada vez mais evidente."
        ),
        WeeklyInfo(
            week = 14,
            title = "Semana 14",
            size = "Limão Siciliano",
            length = "8 - 10 cm",
            weight = "40 - 50 g",
            baby = "O bebê pode fazer expressões faciais e o pescoço está mais definido. Um cabelo fino e macio, chamado lanugo, começa a cobrir o corpo para proteger a pele.",
            mom = "Dores nos ligamentos redondos (fisgadas na virilha) podem ocorrer conforme seu útero se expande. Movimente-se com cuidado."
        ),
        WeeklyInfo(
            week = 15,
            title = "Semana 15",
            size = "Maçã",
            length = "10 - 12 cm",
            weight = "70 - 80 g",
            baby = "Os ossos estão se solidificando e o bebê pode sentir a luz através das pálpebras finas e fechadas. Ele está praticando a respiração com o líquido amniótico.",
            mom = "Seu coração está trabalhando 20% mais para bombear sangue para o bebê. Congestão nasal é comum. Algumas mulheres sentem os primeiros e sutis movimentos do bebê."
        ),
        WeeklyInfo(
            week = 16,
            title = "Semana 16",
            size = "Abacate",
            length = "11 - 13 cm",
            weight = "100 - 120 g",
            baby = "O sistema nervoso continua a se desenvolver. O bebê pode ouvir seus batimentos cardíacos e sua voz. Converse e cante para ele!",
            mom = "Prepare-se para um estirão de crescimento! Sua barriga vai crescer rapidamente nas próximas semanas. Mantenha a pele bem hidratada para ajudar a prevenir estrias."
        ),
        WeeklyInfo(
            week = 17,
            title = "Semana 17",
            size = "Pera",
            length = "13 - 15 cm",
            weight = "140 - 160 g",
            baby = "O esqueleto está mudando de cartilagem para osso. O cordão umbilical fica mais forte e espesso. O bebê começa a acumular gordura para regular a temperatura corporal.",
            mom = "Dores nas costas podem se intensificar. Tente manter uma boa postura e durma com um travesseiro entre as pernas para maior conforto."
        ),
        WeeklyInfo(
            week = 18,
            title = "Semana 18",
            size = "Batata-doce",
            length = "14 - 16 cm",
            weight = "190 - 220 g",
            baby = "As terminações nervosas são cobertas por mielina, acelerando a comunicação entre os neurônios. O sexo do bebê geralmente já pode ser visto claramente no ultrassom.",
            mom = "Os movimentos do bebê estão ficando mais fortes! Você pode sentir tonturas ao se levantar rápido (hipotensão postural). Faça os movimentos de forma suave."
        ),
        WeeklyInfo(
            week = 19,
            title = "Semana 19",
            size = "Manga",
            length = "15 - 17 cm",
            weight = "240 - 280 g",
            baby = "Uma camada gordurosa, chamada vernix caseosa, começa a cobrir a pele do bebê para protegê-lo do líquido amniótico. As áreas do cérebro para os sentidos estão se desenvolvendo.",
            mom = "Manchas escuras na pele (melasma) podem aparecer no rosto. A 'linea nigra', uma linha escura na barriga, também pode surgir. Use protetor solar diariamente."
        ),
        WeeklyInfo(
            week = 20,
            title = "Semana 20",
            size = "Banana",
            length = "16 - 25 cm",
            weight = "300 - 350 g",
            baby = "Metade do caminho! Os sentidos estão se desenvolvendo rapidamente. Ele engole mais líquido amniótico, o que é um ótimo treino para o sistema digestivo.",
            mom = "Parabéns, você chegou à metade da jornada! O ultrassom morfológico desta fase é um marco para verificar a saúde e a anatomia do bebê em detalhes."
        ),
        WeeklyInfo(
            week = 21,
            title = "Semana 21",
            size = "Cenoura Grande",
            length = "26 - 28 cm",
            weight = "360 - 420 g",
            baby = "As sobrancelhas e pálpebras estão totalmente formadas. O bebê tem um ciclo de sono e vigília mais definido, que nem sempre será o mesmo que o seu.",
            mom = "Você provavelmente sentirá os chutes e piruetas com mais clareza. Seu parceiro pode conseguir senti-los colocando a mão na sua barriga!"
        ),
        WeeklyInfo(
            week = 22,
            title = "Semana 22",
            size = "Espiga de Milho",
            length = "27 - 29 cm",
            weight = "430 - 500 g",
            baby = "A pele ainda é fina e translúcida, mas o bebê começa a parecer um recém-nascido em miniatura. Os lábios e olhos estão mais distintos.",
            mom = "Seus pés e tornozelos podem começar a inchar, especialmente no final do dia. Beba bastante água e eleve as pernas sempre que possível."
        ),
        WeeklyInfo(
            week = 23,
            title = "Semana 23",
            size = "Mamão Papaia",
            length = "29 - 31 cm",
            weight = "500 - 600 g",
            baby = "A audição está bem desenvolvida e ele pode se assustar com ruídos altos. Os pulmões continuam a amadurecer, produzindo surfactante.",
            mom = "Contrações de Braxton Hicks (de treinamento) podem começar. São irregulares e indolores, preparando seu útero para o parto."
        ),
        WeeklyInfo(
            week = 24,
            title = "Semana 24",
            size = "Melão Cantaloupe",
            length = "30 - 32 cm",
            weight = "600 - 700 g",
            baby = "O bebê atinge o marco da viabilidade, ou seja, teria chance de sobreviver com ajuda médica se nascesse prematuramente. O rosto está completamente formado.",
            mom = "Seu médico pode pedir o teste de tolerância à glicose para verificar o risco de diabetes gestacional. É um exame de rotina importante."
        ),
        WeeklyInfo(
            week = 25,
            title = "Semana 25",
            size = "Couve-flor",
            length = "34 - 36 cm",
            weight = "660 - 780 g",
            baby = "O bebê está ganhando mais gordura, o que torna sua pele menos enrugada. O cabelo real começa a crescer e a ganhar cor. As narinas começam a se abrir.",
            mom = "A azia pode se tornar um incômodo frequente. Evite alimentos gordurosos ou picantes e não se deite logo após as refeições."
        ),
        WeeklyInfo(
            week = 26,
            title = "Semana 26",
            size = "Alface Americana",
            length = "35 - 37 cm",
            weight = "760 - 900 g",
            baby = "As redes neurais do cérebro relacionadas à audição e visão já estão funcionando. Ele responde a estímulos como luz e som.",
            mom = "A pressão na bexiga está de volta com força total. Dores de cabeça também podem ser mais frequentes. Converse com seu médico sobre medicamentos seguros."
        ),
        WeeklyInfo(
            week = 27,
            title = "Semana 27",
            size = "Berinjela",
            length = "36 - 38 cm",
            weight = "870 g - 1 kg",
            baby = "O bebê abre e fecha os olhos, dorme e acorda em intervalos regulares e pode até chupar o dedo. O cérebro está muito ativo.",
            mom = "Fim do segundo trimestre! A síndrome das pernas inquietas e cãibras podem atrapalhar seu sono. Tente se alongar antes de dormir."
        ),
        WeeklyInfo(
            week = 28,
            title = "Semana 28",
            size = "Repolho",
            length = "37 - 39 cm",
            weight = "1 kg - 1,2 kg",
            baby = "Bem-vindo ao terceiro trimestre! O bebê pode reconhecer sua voz e a do parceiro. A visão está melhorando e ele pisca.",
            mom = "As visitas ao pré-natal se tornarão mais frequentes. É um bom momento para começar a pesquisar sobre parto, amamentação e cuidados com o recém-nascido."
        ),
        WeeklyInfo(
            week = 29,
            title = "Semana 29",
            size = "Abóbora Moranga",
            length = "38 - 40 cm",
            weight = "1,2 kg - 1,4 kg",
            baby = "Os músculos e pulmões continuam a amadurecer. A cabeça está crescendo para acomodar o cérebro em rápido desenvolvimento.",
            mom = "Falta de ar e cansaço podem aumentar, pois o útero pressiona o diafragma. Tente dormir de lado, de preferência o esquerdo, para melhorar a circulação."
        ),
        WeeklyInfo(
            week = 30,
            title = "Semana 30",
            size = "Pepino Grande",
            length = "39 - 41 cm",
            weight = "1,3 kg - 1,5 kg",
            baby = "A medula óssea assumiu completamente a produção de glóbulos vermelhos. Seus olhos conseguem focar. Ele está ganhando peso rapidamente.",
            mom = "A 'névoa cerebral da gravidez' pode ser real! Esquecimentos podem acontecer. Anote seus compromissos e não se cobre tanto."
        ),
        WeeklyInfo(
            week = 31,
            title = "Semana 31",
            size = "Coco",
            length = "41 - 43 cm",
            weight = "1,5 kg - 1,7 kg",
            baby = "O sistema nervoso central está maduro o suficiente para controlar a temperatura corporal. O bebê ouve, sente e vê (dentro das limitações do útero).",
            mom = "O colostro, o primeiro leite rico em anticorpos, pode começar a vazar dos seios. Use protetores de seio se isso te incomodar."
        ),
        WeeklyInfo(
            week = 32,
            title = "Semana 32",
            size = "Inhame",
            length = "42 - 44 cm",
            weight = "1,7 kg - 1,9 kg",
            baby = "A camada de lanugo começa a cair. As unhas das mãos e dos pés estão completamente formadas. O bebê provavelmente já está de cabeça para baixo.",
            mom = "Seu corpo está se preparando para o parto. Você pode sentir mais pressão na pelve. O volume sanguíneo aumentou quase 50% desde o início da gravidez."
        ),
        WeeklyInfo(
            week = 33,
            title = "Semana 33",
            size = "Abacaxi",
            length = "43 - 45 cm",
            weight = "1,9 kg - 2,2 kg",
            baby = "Os ossos do crânio do bebê ainda não estão fundidos, o que permite que se movam para facilitar a passagem pelo canal de parto.",
            mom = "A dificuldade para encontrar uma posição confortável para dormir é um grande desafio. Use muitos travesseiros para apoiar a barriga e as costas."
        ),
        WeeklyInfo(
            week = 34,
            title = "Semana 34",
            size = "Melão Amarelo",
            length = "45 - 46 cm",
            weight = "2,1 kg - 2,4 kg",
            baby = "Os pulmões estão quase totalmente desenvolvidos. A camada de vernix caseosa fica mais espessa. O sistema imunológico do bebê está recebendo seus anticorpos.",
            mom = "Sua visão pode ficar um pouco embaçada devido à retenção de líquidos. A fadiga do terceiro trimestre pode ser intensa; descanse sempre que puder."
        ),
        WeeklyInfo(
            week = 35,
            title = "Semana 35",
            size = "Mamão Formosa",
            length = "46 - 47 cm",
            weight = "2,4 kg - 2,7 kg",
            baby = "O bebê tem menos espaço, então os movimentos mudam, mas a frequência deve ser a mesma. O desenvolvimento físico está quase completo.",
            mom = "É hora de finalizar a mala da maternidade e discutir seu plano de parto com o médico. A ansiedade pode aumentar. Converse sobre seus medos e expectativas."
        ),
        WeeklyInfo(
            week = 36,
            title = "Semana 36",
            size = "Um pé de Alface Romana",
            length = "47 - 48 cm",
            weight = "2,5 kg - 2,9 kg",
            baby = "Considerado 'termo inicial'. O bebê desce e se encaixa na pelve, o que pode aliviar a pressão nos seus pulmões. Ele está treinando a sucção.",
            mom = "Com o encaixe do bebê, a pressão na bexiga aumenta, mas você pode sentir um alívio na respiração e na azia. As consultas pré-natais agora são semanais."
        ),
        WeeklyInfo(
            week = 37,
            title = "Semana 37",
            size = "Abóbora Paulistinha",
            length = "48 - 49 cm",
            weight = "2,7 kg - 3,1 kg",
            baby = "O bebê pratica a respiração e está pronto para a vida fora do útero. O cérebro e os pulmões são os últimos órgãos a amadurecer completamente.",
            mom = "Fique atenta aos sinais de trabalho de parto: contrações regulares, perda do tampão mucoso e o rompimento da bolsa."
        ),
        WeeklyInfo(
            week = 38,
            title = "Semana 38",
            size = "Melancia Pequena",
            length = "49 - 50 cm",
            weight = "2,8 kg - 3,3 kg",
            baby = "O bebê tem uma pegada firme e a cor dos seus olhos ao nascer pode não ser a definitiva, podendo mudar nos primeiros meses de vida.",
            mom = "Tente descansar o máximo possível. A 'síndrome do ninho arrumado', uma vontade súbita de limpar e organizar tudo, pode aparecer. Não se esgote!"
        ),
        WeeklyInfo(
            week = 39,
            title = "Semana 39",
            size = "Jaca Pequena",
            length = "50 - 51 cm",
            weight = "3,0 kg - 3,5 kg",
            baby = "Considerado 'termo completo'! O bebê continua a acumular gordura para ajudar a controlar a temperatura corporal após o nascimento.",
            mom = "A qualquer momento agora! Caminhadas leves podem ajudar a estimular o trabalho de parto. Confie no seu corpo e mantenha a calma."
        ),
        WeeklyInfo(
            week = 40,
            title = "Semana 40",
            size = "Abóbora Grande",
            length = "51 - 52 cm",
            weight = "3,2 kg - 3,7 kg",
            baby = "A data provável do parto chegou! Mas não se preocupe, é apenas uma estimativa. Ele está confortável e pronto para conhecer o mundo.",
            mom = "Apenas uma pequena porcentagem dos bebês nasce na data exata. Continue monitorando os movimentos do bebê e converse com seu médico."
        ),
        WeeklyInfo(
            week = 41,
            title = "Semana 41",
            size = "Uma grande Abóbora Moranga",
            length = "51,5 - 52,5 cm",
            weight = "3,3 kg - 3,9 kg",
            baby = "Seu bebê é considerado 'termo tardio'. Ele está totalmente desenvolvido e pode ter a pele um pouco seca ou descamada ao nascer.",
            mom = "Seu médico monitorará você e o bebê de perto. A indução do parto pode ser discutida como uma opção segura."
        ),
        WeeklyInfo(
            week = 42,
            title = "Semana 42",
            size = "Uma Melancia Gigante",
            length = "52 - 53 cm",
            weight = "3,4 kg - 4 kg",
            baby = "Se você chegou até aqui, seu bebê é 'pós-termo'. Ele está mais alerta e pode ter unhas mais compridas ao nascer. Está pronto para conhecer o mundo.",
            mom = "A espera está no fim! A indução do parto é geralmente recomendada nesta semana para garantir a saúde e a segurança de ambos."
        )
    )

    fun getInfoForWeek(week: Int): WeeklyInfo? {
        // Ajuste para garantir que a semana 1 corresponda ao primeiro item, e assim por diante.
        if (week in 1..weeklyInfoList.size) {
            return weeklyInfoList[week - 1]
        }
        return null // Retorna nulo se a semana estiver fora do intervalo
    }
}
package br.com.gestahub.ui.maternitybag

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.gestahub.ui.components.Header

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaternityBagScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean, // Adicionado parâmetro para o tema
    viewModel: MaternityBagViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Header(
                title = "Mala Maternidade",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.listData != null -> {
                    MaternityBagContent(
                        listData = uiState.listData!!,
                        checkedItems = uiState.checkedItems,
                        onToggleItem = viewModel::toggleItem,
                        isDarkTheme = isDarkTheme // Passa o tema para o conteúdo
                    )
                }
            }
        }
    }
}

@Composable
fun MaternityBagContent(
    listData: MaternityBagList,
    checkedItems: List<String>,
    onToggleItem: (String) -> Unit,
    isDarkTheme: Boolean // Adicionado parâmetro para o tema
) {
    val categories = listOf(
        listData.mom,
        listData.baby,
        listData.companion,
        listData.docs
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories.filter { it.items.isNotEmpty() }, key = { it.title }) { category ->
            CategoryCard(
                category = category,
                checkedItems = checkedItems,
                onToggleItem = onToggleItem,
                isDarkTheme = isDarkTheme // Passa o tema para o card
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: MaternityBagCategory,
    checkedItems: List<String>,
    onToggleItem: (String) -> Unit,
    isDarkTheme: Boolean // Adicionado parâmetro para o tema
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            // --- ATUALIZAÇÃO APLICADA AQUI ---
            // Usa o parâmetro isDarkTheme ao invés da verificação do sistema
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            category.items.forEach { item ->
                ChecklistItem(
                    item = item,
                    isChecked = checkedItems.contains(item.id),
                    onToggle = { onToggleItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun ChecklistItem(
    item: MaternityBagItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}
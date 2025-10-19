package br.com.gestahub.ui.shoppinglist

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.gestahub.ui.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingListViewModel = viewModel(),
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val listData by viewModel.listData
    val checkedItems by viewModel.checkedItems
    val isLoading by viewModel.loading

    var showAddItemDialog by remember { mutableStateOf<String?>(null) }
    var newItemLabel by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }

    var itemToDelete by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    if (itemToDelete != null) {
        ConfirmationDialog(
            title = "Confirmar Exclusão",
            text = "Tem certeza que deseja remover o item \"${itemToDelete!!.third}\"?",
            confirmButtonText = "Excluir",
            onConfirm = {
                itemToDelete?.let { (categoryId, itemId, _) ->
                    viewModel.removeItem(categoryId, itemId)
                }
                itemToDelete = null
            },
            onDismissRequest = { itemToDelete = null }
        )
    }

    if (showAddItemDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = null },
            title = { Text("Adicionar Item") },
            text = {
                OutlinedTextField(
                    value = newItemLabel,
                    onValueChange = { newItemLabel = it },
                    label = { Text("Nome do item") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newItemLabel.isNotBlank()) {
                        viewModel.addItem(showAddItemDialog!!, newItemLabel)
                        newItemLabel = ""
                        showAddItemDialog = null
                    }
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = null }) { Text("Cancelar") }
            }
        )
    }

    if (showRestoreDialog) {
        ConfirmationDialog(
            title = "Restaurar Lista",
            text = "Tem certeza de que deseja restaurar os itens padrão da lista de compras? Itens personalizados não serão afetados.",
            onConfirm = {
                viewModel.restoreDefaults()
                showRestoreDialog = false
            },
            onDismissRequest = { showRestoreDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showRestoreDialog = true }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar Padrão")
                    }
                }
            )
        },
    ) { paddingValues ->
        // 1. CORREÇÃO DE ESPAÇAMENTO: Adicionado Surface para aplicar o padding corretamente
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), // Padding de 16dp em todos os lados do conteúdo da lista
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listData?.forEach { (categoryId, category) ->
                        item {
                            ShoppingListCategoryCard(
                                categoryId = categoryId,
                                category = category,
                                checkedItems = checkedItems,
                                onToggleItem = { viewModel.toggleItem(it) },
                                onAddItemRequest = { showAddItemDialog = categoryId },
                                onRemoveRequest = { catId, itemId, itemLabel ->
                                    itemToDelete = Triple(catId, itemId, itemLabel)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListCategoryCard(
    categoryId: String,
    category: ShoppingListCategory,
    checkedItems: List<String>,
    onToggleItem: (String) -> Unit,
    onAddItemRequest: () -> Unit,
    onRemoveRequest: (categoryId: String, itemId: String, itemLabel: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onAddItemRequest) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Item")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            category.items.forEach { item ->
                ItemRow(
                    item = item,
                    isChecked = checkedItems.contains(item.id),
                    onToggle = { onToggleItem(item.id) },
                    onRemove = { onRemoveRequest(categoryId, item.id, item.label) }
                )
            }
        }
    }
}

@Composable
fun ItemRow(
    item: ShoppingListItem,
    isChecked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    val textColor = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

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
            ),
            modifier = Modifier.scale(1.2f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            // 2. CORREÇÃO VISUAL: Removido o 'textDecoration'
            modifier = Modifier.weight(1f)
        )
        if (item.isCustom) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover Item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
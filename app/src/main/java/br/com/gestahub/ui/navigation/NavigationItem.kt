package br.com.gestahub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val navItems = listOf(
    NavItem("Início", Icons.Default.Home, "home"),
    NavItem("Consultas", Icons.Default.CalendarMonth, "appointments"),
    NavItem("Diário", Icons.Default.Book, "journal"),
    NavItem("Peso", Icons.Default.BarChart, "weight"),
    NavItem("Mais", Icons.Default.MoreHoriz, "more")
)
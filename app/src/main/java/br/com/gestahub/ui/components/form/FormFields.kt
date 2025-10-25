// Local: app/src/main/java/br/com/gestahub/ui/components/form/FormFields.kt
package br.com.gestahub.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Um campo de texto genérico e reutilizável que abre um DatePickerDialog.
 * Ele lida com a formatação da data para exibição (dd/MM/yyyy) e para o banco de dados (yyyy-MM-dd).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateString: String, // Espera a data no formato "yyyy-MM-dd"
    onDateSelected: (String) -> Unit, // Retorna a data no formato "yyyy-MM-dd"
    modifier: Modifier = Modifier,
    selectableDates: SelectableDates = object : SelectableDates {} // Permite todas as datas por padrão
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR")) }
    val dbFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE } // yyyy-MM-dd

    val dateForDisplay = remember(dateString) {
        if (dateString.isNotBlank()) {
            try {
                LocalDate.parse(dateString, dbFormatter).format(displayFormatter)
            } catch (e: Exception) { "" }
        } else { "" }
    }

    Box(modifier = modifier.clickable { showDatePicker = true }) {
        OutlinedTextField(
            value = dateForDisplay,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Abrir calendário")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (dateString.isNotBlank()) {
                try {
                    LocalDate.parse(dateString, dbFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e: Exception) { Instant.now().toEpochMilli() }
            } else { Instant.now().toEpochMilli() },
            selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            onDateSelected(selectedDate.format(dbFormatter))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Um campo de texto genérico e reutilizável que abre um TimePickerDialog.
 */
@Composable
fun TimePickerField(
    label: String,
    timeString: String, // Espera a hora no formato "HH:mm"
    onTimeSelected: (String) -> Unit, // Retorna a hora no formato "HH:mm"
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.clickable { showTimePicker = true }) {
        OutlinedTextField(
            value = timeString,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("HH:MM") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.WatchLater, contentDescription = "Abrir seletor de hora")
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showTimePicker) {
        val time = if (timeString.isNotBlank()) {
            runCatching { LocalTime.parse(timeString) }.getOrDefault(LocalTime.now())
        } else {
            LocalTime.now()
        }
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { selectedTime ->
                onTimeSelected(selectedTime)
                showTimePicker = false
            },
            initialTime = time
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialTime: LocalTime
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    TextButton(onClick = {
                        val selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        onConfirm(selectedTime)
                    }) { Text("OK") }
                }
            }
        }
    }
}
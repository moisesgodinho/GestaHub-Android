// Local: app/src/main/java/br/com/gestahub/ui/medicationtracker/MedicationViewModelFactory.kt
package br.com.gestahub.ui.medicationtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDate

@Suppress("UNCHECKED_CAST")
class MedicationViewModelFactory(
    private val estimatedLmp: LocalDate?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            return MedicationViewModel(estimatedLmp) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
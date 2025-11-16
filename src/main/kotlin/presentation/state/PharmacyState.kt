package presentation.state

import domain.entities.InventoryItem
import domain.entities.Prescription

data class PharmacyState(
    val pendingPrescription: List<Prescription> = emptyList(),
    val lowStockItem: List<InventoryItem> = emptyList(),
    val selectedPrescription: Prescription? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

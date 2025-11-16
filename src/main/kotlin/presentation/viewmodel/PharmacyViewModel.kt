package presentation.viewmodel

import core.utils.PrescriptionId
import data.repoistories.InventoryRepository
import data.repoistories.PrescriptionRepository
import domain.entities.Prescription
import domain.usecases.implementation.DispensePrescriptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.state.PharmacyState
import presentation.state.UiEvent

class PharmacyViewModel(
    private val pharmacistId: String,
    private val prescriptionRepository: PrescriptionRepository,
    private val inventoryRepository: InventoryRepository,
    private val dispensePrescription: DispensePrescriptionUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(PharmacyState())
    val state = _state.asStateFlow()

    init {
        loadPharmacyDate()
    }

    private fun loadPharmacyDate() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val pendingResult = prescriptionRepository.findPendingForDispensing()
            val lowStockResult = inventoryRepository.findLowStock()

            _state.value = PharmacyState(
                pendingPrescription = pendingResult.getOrNull().orEmpty(),
                lowStockItem = lowStockResult.getOrNull().orEmpty(),
                isLoading = false
            )
        }
    }

    fun selectedPrescription(prescription: Prescription) {
        _state.value = _state.value.copy(selectedPrescription = prescription)
    }

    fun dispensePrescription(prescriptionId: PrescriptionId) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val input = DispensePrescriptionUseCase.Input(
                prescriptionId = prescriptionId,
                pharmacistId = pharmacistId
            )

            dispensePrescription(input).fold(
                onSuccess = {
                    emitEvent(UiEvent.ShowToast("Prescription dispensed successfully"))
                    loadPharmacyDate()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    emitEvent(UiEvent.ShowError(error))
                }
            )
        }
    }
}
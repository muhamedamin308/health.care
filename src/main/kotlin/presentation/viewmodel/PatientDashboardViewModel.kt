package presentation.viewmodel

import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.InvoiceRepository
import data.repoistories.PrescriptionRepository
import domain.models.InvoiceStatus
import domain.models.PrescriptionStatus
import domain.usecases.implementation.GetPatientAppointmentsUseCase
import domain.usecases.implementation.GetPatientProfileUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.state.PatientDashboardState
import presentation.state.UiEvent

class PatientDashboardViewModel(
    private val patientId: PatientId,
    private val getPatientProfile: GetPatientProfileUseCase,
    private val getPatientAppointments: GetPatientAppointmentsUseCase,
    private val prescriptionRepository: PrescriptionRepository,
    private val invoiceRepository: InvoiceRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(PatientDashboardState())
    val state: StateFlow<PatientDashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val patientDeferred = async { getPatientProfile(patientId) }
                val appointmentDeferred = async {
                    getPatientAppointments(
                        GetPatientAppointmentsUseCase.Input(
                            patientId = patientId,
                            upcomingOnly = true
                        )
                    )
                }
                val prescriptionDeferred = async {
                    prescriptionRepository.findByPatient(patientId)
                }
                val invoiceDeferred = async {
                    invoiceRepository.findByPatient(patientId)
                }
                val patient = patientDeferred.await().getOrNull()
                val appointment = appointmentDeferred.await().getOrNull().orEmpty()
                val prescriptions = prescriptionDeferred.await().getOrNull().orEmpty()
                val invoices = invoiceDeferred.await().getOrNull().orEmpty()

                val recentPrescription = prescriptions
                    .filter { it.status == PrescriptionStatus.PENDING }
                    .sortedByDescending { it.createdAt }
                    .take(6)

                val pendingInvoices = invoices.filter {
                    it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE
                }

                _state.value = PatientDashboardState(
                    patient = patient,
                    upcomingAppointments = appointment,
                    recentPrescription = recentPrescription,
                    pendingInvoices = pendingInvoices,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = DomainError.SystemError("Failed to load dashboard", cause = e)
                )
            }
        }
    }

    fun navigateToAppointments() {
        viewModelScope.launch {
            emitEvent(UiEvent.Navigate("/appointments"))
        }
    }

    fun navigateToPrescriptions() {
        viewModelScope.launch {
            emitEvent(UiEvent.Navigate("/prescriptions"))
        }
    }
}
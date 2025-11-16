package presentation.state

import core.functional.Money

data class AnalyticsDashboardState(
    val totalPatients: Int = 0,
    val appointmentsToday: Int = 0,
    val completedToday: Int = 0,
    val pendingAppointments: Int = 0,
    val totalRevenue: Money = Money(0.0),
    val outstandingPayments: Money = Money(0.0),
    val isLoading: Boolean = false,
    val error: String? = null,
)
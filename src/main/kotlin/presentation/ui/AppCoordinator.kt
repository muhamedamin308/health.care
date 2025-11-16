package presentation.ui

class AppCoordinator {
    private var currentScreen: Screen = Screen.Login

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        println("📱 Navigated to: ${screen::class.simpleName}")
    }

    fun getCurrentScreen() = currentScreen
}
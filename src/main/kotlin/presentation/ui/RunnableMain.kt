package presentation.ui

suspend fun main() {
    // Start the interactive UI
    val mainMenu = MainMenuUI()
    mainMenu.start()

    // Start the automated Demo
//    runAutomatedDemo()
}
package presentation.ui

object Colors {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    fun success(text: String) = "$GREEN$text$RESET"
    fun error(text: String) = "$RED$text$RESET"
    fun warning(text: String) = "$YELLOW$text$RESET"
    fun info(text: String) = "$CYAN$text$RESET"
    fun highlight(text: String) = "$PURPLE$text$RESET"
}
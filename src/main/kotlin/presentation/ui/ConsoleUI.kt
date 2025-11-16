package presentation.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConsoleUI {

    fun clearScreen() {
        print("\u001B[H\u001B[2J")
        System.out.flush()
    }

    fun printHeader(title: String) {
        println("\n${"=".repeat(70)}")
        println(Colors.highlight("  $title"))
        println("=".repeat(70))
    }

    fun printSubHeader(title: String) {
        println("\n${Colors.info("▶ $title")}")
        println("-".repeat(70))
    }

    fun printSuccess(message: String) {
        println(Colors.success("✓ $message"))
    }

    fun printError(message: String) {
        println(Colors.error("✗ $message"))
    }

    fun printWarning(message: String) {
        println(Colors.warning("⚠ $message"))
    }

    fun printInfo(message: String) {
        println(Colors.info("ℹ $message"))
    }

    fun prompt(message: String): String {
        print("${Colors.info("➤")} $message: ")
        return readlnOrNull() ?: ""
    }

    fun promptInt(message: String): Int? {
        return try {
            prompt(message).toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun promptDate(message: String): LocalDate? {
        return try {
            val input = prompt(message)
            LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            printError("Invalid date format. Use YYYY-MM-DD")
            null
        }
    }

    fun promptDateTime(message: String): LocalDateTime? {
        return try {
            val input = prompt(message)
            LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            printError("Invalid datetime format. Use YYYY-MM-DDTHH:mm")
            null
        }
    }

    fun waitForEnter(message: String = "Press ENTER to continue...") {
        print("\n${Colors.info(message)}")
        readlnOrNull()
    }

    fun printMenu(title: String, options: List<String>) {
        printSubHeader(title)
        options.forEachIndexed { index, option ->
            println("  ${index + 1}. $option")
        }
        println("  0. ${Colors.warning("Back/Exit")}")
    }
}
package core.functional

inline fun <T> retry(
    times: Int = 3,
    delayMillis: Long = 1000,
    block: () -> T
): T {
    var lastException: Exception? = null
    repeat(times) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (attempt < times - 1)
                Thread.sleep(delayMillis)
        }
    }

    throw lastException ?: Exception("Retry failed")
}
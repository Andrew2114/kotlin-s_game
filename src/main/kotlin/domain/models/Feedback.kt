package domain.models

data class Feedback(val blackPins: Int, val whitePins: Int) {
    fun isWinning() = blackPins == whitePins
}
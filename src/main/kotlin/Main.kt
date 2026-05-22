import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.*
import infrastructure.rules.MastermindRulesImpl
import infrastructure.repositories.InMemoryGameRepository
import domain.rules.MastermindRules.Companion.MAX_MOVES
import domain.rules.MastermindRules.Companion.CODE_LENGTH
import presentation.console.MastermindConsole

fun main() {
    println("Hello world from Kotlin's Game!")
    val repository = InMemoryGameRepository()
    val rules = MastermindRulesImpl()
    val gameUseCases = GameUseCases(rules, repository)
    val statisticsUseCases = StatisticsUseCases(repository)

    val console = MastermindConsole(gameUseCases, statisticsUseCases)
    console.start()
}

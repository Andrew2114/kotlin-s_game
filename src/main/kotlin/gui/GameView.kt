package gui

import application.usecases.GameUseCases
import domain.models.*
import domain.rules.MastermindRules.Companion.CODE_LENGTH
import domain.rules.MastermindRules.Companion.MAX_MOVES
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color as JavaFxColor
import javafx.scene.shape.Rectangle

class GameView(
    private val gameUseCases: GameUseCases,
    private val player1: Player,
    private val player2: Player,
    private val onPlayerSwitch: () -> Unit
) {
    val root = BorderPane()

    private var currentGame: Game? = null
    private var currentPlayer = player1
    private var currentPlayerName = player1.name
    private var currentSecret: Combination? = null

    private val movesList = ListView<String>()
    private val feedbackLabel = Label("")
    private val movesCountLabel = Label("Ходов: 0")
    private val statusLabel = Label("")
    private val currentPlayerLabel = Label("Ходит: ${player1.name}")

    private val selectedColors = mutableListOf<Color?>()
    private lateinit var colorPositions: List<ColorPosition>
    private val colorButtons = mutableListOf<Button>()

    init {
        setupUI()
        startNewGame()
    }

    private fun setupUI() {
        val topPanel = createTopPanel()
        root.top = topPanel

        val centerPanel = createCenterPanel()
        root.center = centerPanel

        val bottomPanel = createBottomPanel()
        root.bottom = bottomPanel

        root.padding = Insets(10.0)
    }

    private fun createTopPanel(): VBox {
        val vbox = VBox(10.0)

        val titleLabel = Label("Игровое поле")
        titleLabel.style = "-fx-font-weight: bold; -fx-font-size: 16px;"

        currentPlayerLabel.style = "-fx-font-weight: bold; -fx-text-fill: #0066CC;"

        val infoPanel = HBox(20.0)
        infoPanel.children.addAll(currentPlayerLabel, movesCountLabel, statusLabel, feedbackLabel)

        val newGameButton = Button("Новая игра")
        newGameButton.setOnAction { startNewGame() }

        vbox.children.addAll(titleLabel, infoPanel, newGameButton)
        return vbox
    }

    private fun createCenterPanel(): VBox {
        val vbox = VBox(10.0)

        val label = Label("История ходов")
        label.style = "-fx-font-weight: bold;"

        movesList.prefHeight = 300.0

        vbox.children.addAll(label, movesList)
        return vbox
    }

    private fun createBottomPanel(): VBox {
        val vbox = VBox(15.0)

        val colorPanel = HBox(10.0)
        colorPanel.alignment = Pos.CENTER

        val positions = mutableListOf<ColorPosition>()

        for (i in 0 until CODE_LENGTH) {
            val position = ColorPosition(i)
            positions.add(position)
            colorPanel.children.add(position)
        }
        colorPositions = positions

        val availableColors = HBox(10.0)
        availableColors.alignment = Pos.CENTER

        for (color in Color.entries) {
            val colorButton = createColorButton(color)
            colorButtons.add(colorButton)
            availableColors.children.add(colorButton)
        }

        val submitButton = Button("Сделать ход")
        submitButton.setOnAction {
            makeMove()
        }

        vbox.children.addAll(
            colorPanel,
            Label("Доступные цвета:"),
            availableColors,
            submitButton
        )

        return vbox
    }

    private fun isGameActive(): Boolean {
        return currentGame != null && currentGame?.status == GameStatus.IN_PROGRESS
    }

    private fun setControlsEnabled(enabled: Boolean) {
        colorButtons.forEach { it.isDisable = !enabled }
        if (::colorPositions.isInitialized) {
            colorPositions.forEach { it.locked = !enabled }
        }
    }

    private fun switchPlayer() {
        if (!isGameActive()) return

        currentPlayer = if (currentPlayer == player1) player2 else player1
        currentPlayerName = currentPlayer.name
        currentPlayerLabel.text = "Ходит: ${currentPlayer.name}"
        onPlayerSwitch()
        clearSelectedColors()
    }

    private fun askSecretCombination(): Combination? {
        val dialog = TextInputDialog()
        dialog.title = "Секретная комбинация"
        dialog.headerText = "Введите секретную комбинацию из 4 цветов"
        dialog.contentText = "Доступные цвета: ${Color.entries.joinToString { it.name }}\nПример: RED,GREEN,BLUE,YELLOW"

        val result = dialog.showAndWait()
        val input = result.orElse(null)?.trim()?.uppercase()

        if (input.isNullOrBlank()) return null

        val parts = input.split(",").map { it.trim() }
        if (parts.size != 4) {
            showError("Нужно ввести ровно 4 цвета!")
            return null
        }

        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }

        if (colors.size != 4) {
            showError("Один из цветов не распознан")
            return null
        }

        return Combination(colors)
    }

    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Ошибка"
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

    private fun createColorButton(color: Color): Button {
        val button = Button(color.name)
        button.style = "-fx-background-color: ${getColorStyle(color)}; -fx-text-fill: white; -fx-font-weight: bold;"
        button.prefWidth = 80.0

        button.setOnAction {
            if (!isGameActive()) {
                feedbackLabel.text = "Игра уже закончена! Нажмите 'Новая игра'."
                return@setOnAction
            }
            for (i in 0 until CODE_LENGTH) {
                if (selectedColors.getOrNull(i) == null) {
                    selectedColors.add(i, color)
                    updateColorPosition(i, color)
                    break
                }
            }
        }

        return button
    }

    private fun updateColorPosition(position: Int, color: Color) {
        if (::colorPositions.isInitialized && position < colorPositions.size) {
            colorPositions[position].setColor(color)
        }
    }

    private fun makeMove() {
        if (!isGameActive()) {
            feedbackLabel.text = "Игра уже закончена! Нажмите 'Новая игра'."
            return
        }

        val colors = selectedColors.mapNotNull { it }
        if (colors.size != CODE_LENGTH) {
            feedbackLabel.text = "Выберите все 4 цвета!"
            return
        }

        val current = currentGame
        if (current == null) {
            feedbackLabel.text = "Сначала начните новую игру!"
            return
        }

        val guess = Combination(colors)

        try {
            val move = gameUseCases.makeMove(current.id, currentPlayer.id, guess)
            currentGame = gameUseCases.findById(current.id)

            val moveText = "Ход ${move.moveNumber} (${currentPlayerName}): ${colors.joinToString { it.name }} → Ч:${move.feedback.blackPins} Б:${move.feedback.whitePins}"
            movesList.items.add(moveText)

            feedbackLabel.text = "Результат: чёрных ${move.feedback.blackPins}, белых ${move.feedback.whitePins}"
            movesCountLabel.text = "Ходов: ${currentGame?.moves?.size ?: 0}"

            clearSelectedColors()

            when {
                move.feedback.blackPins == CODE_LENGTH -> {
                    statusLabel.text = "ПОБЕДА! Победил ${currentPlayerName}!"
                    feedbackLabel.text = "Комбинация отгадана за ${move.moveNumber} ходов!"
                    currentGame = null
                    setControlsEnabled(false)
                }
                (currentGame?.moves?.size ?: 0) >= MAX_MOVES -> {
                    statusLabel.text = "ПОРАЖЕНИЕ! Игроки не отгадали комбинацию"
                    feedbackLabel.text = "Секретная комбинация: ${currentSecret?.colors?.joinToString { it.name }}"
                    currentGame = null
                    setControlsEnabled(false)
                }
                else -> {
                    statusLabel.text = "В процессе"
                    switchPlayer()
                }
            }

        } catch (e: Exception) {
            feedbackLabel.text = "Ошибка: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun clearSelectedColors() {
        selectedColors.clear()
        for (i in 0 until CODE_LENGTH) {
            if (::colorPositions.isInitialized && i < colorPositions.size) {
                colorPositions[i].clearColor()
            }
        }
    }

    private fun startNewGame() {
        val secret = askSecretCombination()
        if (secret == null) {
            feedbackLabel.text = "Новая игра не создана: комбинация не введена"
            return
        }
        currentSecret = secret

        currentGame = gameUseCases.createGame(
            player1.id, player1.name,
            player2.id, player2.name,
            secret
        )

        currentPlayer = player1
        currentPlayerName = player1.name
        currentPlayerLabel.text = "Ходит: ${currentPlayer.name}"

        clearSelectedColors()
        movesList.items.clear()
        feedbackLabel.text = "Игра создана! Секретная комбинация задана администратором."
        statusLabel.text = "В процессе"
        movesCountLabel.text = "Ходов: 0"

        setControlsEnabled(true)
    }

    private fun getColorStyle(color: Color): String {
        return when (color) {
            Color.RED -> "#FF0000"
            Color.GREEN -> "#00AA00"
            Color.BLUE -> "#0000FF"
            Color.YELLOW -> "#DDDD00"
            Color.ORANGE -> "#FF8800"
            Color.PURPLE -> "#8800FF"
        }
    }

    inner class ColorPosition(private val index: Int) : StackPane() {
        private val colorRect = Rectangle(40.0, 40.0)
        private val label = Label("?")
        var locked: Boolean = false
            set(value) {
                field = value
                opacity = if (value) 0.5 else 1.0
            }

        init {
            colorRect.fill = JavaFxColor.GRAY
            colorRect.arcWidth = 10.0
            colorRect.arcHeight = 10.0
            colorRect.stroke = JavaFxColor.BLACK
            colorRect.strokeWidth = 1.0

            label.style = "-fx-font-size: 16px; -fx-font-weight: bold;"

            children.addAll(colorRect, label)

            setOnMouseClicked {
                if (locked) return@setOnMouseClicked
                if (!isGameActive()) return@setOnMouseClicked
                selectedColors[index] = null
                colorRect.fill = JavaFxColor.GRAY
                label.text = "?"
            }
        }

        fun setColor(color: Color) {
            if (locked) return
            colorRect.fill = JavaFxColor.web(getColorStyle(color))
            label.text = color.name.take(1)
        }

        fun clearColor() {
            if (locked) return
            colorRect.fill = JavaFxColor.GRAY
            label.text = "?"
        }
    }
}
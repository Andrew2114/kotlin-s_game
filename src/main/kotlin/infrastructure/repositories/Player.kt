package infrastructure.repositories

data class Player(
    val id: String,
    val name: String,
    val createdAt: Long? = null
)
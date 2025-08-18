package whattoeat.dinner.ui.Meals

data class Food(
    var name: String,
    val calories: Int,
    val proteins: Double,
    val zsir: Double,
    val rost: Double,
    val szenhidrat: Double,
    var count: Int = 1
)
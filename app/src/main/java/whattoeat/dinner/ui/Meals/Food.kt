package whattoeat.dinner.ui.Meals

data class Food(
    var name: String,
    val calories: Int,
    val proteins: Double,
    val zsir: Double,
    val rost: Double,
    val szenhidrat: Double,
    var count: Double = 1.0,
    var weight: Double = 100.0  // Base weight in grams (default 100g)
)
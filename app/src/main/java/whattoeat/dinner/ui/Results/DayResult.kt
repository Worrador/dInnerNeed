package whattoeat.dinner.ui.Results

class DayResult (year : Int, month : Int, day : Int, scoredCalories : String, scoredProteins : String, scoredZsir : String, scoredRost : String, scoredSzenhidrat : String, isSuccess : Boolean) {
    var year : Int
    var month : Int
    var day : Int
    var scoredCalories : String
    var scoredProteins : String
    var scoredZsir : String
    var scoredRost : String
    var scoredSzenhidrat : String
    var isSuccess : Boolean

    init{
        this.year = year
        this.month = month
        this.day = day
        this.scoredCalories = scoredCalories
        this.scoredProteins = scoredProteins
        this.scoredZsir = scoredZsir
        this.scoredRost = scoredRost
        this.scoredSzenhidrat = scoredSzenhidrat
        this.isSuccess = isSuccess
    }

}
package whattoeat.dinner.ui.Results

import java.util.*

class DayResult (year : Int, month : Byte, day : Byte, scoredCalories : String, scoredProteins : String, isSuccess : Boolean) {
    var year : Int
    var month : Byte
    var day : Byte
    var scoredCalories : String
    var scoredProteins : String
    var isSuccess : Boolean

    init{
        this.year = year
        this.month = month
        this.day = day
        this.scoredCalories = scoredCalories
        this.scoredProteins = scoredProteins
        this.isSuccess = isSuccess
    }

}
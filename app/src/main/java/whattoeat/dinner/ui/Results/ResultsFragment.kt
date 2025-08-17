package whattoeat.dinner.ui.Results

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import whattoeat.dinner.MainActivity
import whattoeat.dinner.R
import whattoeat.dinner.databinding.FragmentResultsBinding
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.ui.Meals.Food
import java.util.*
import kotlin.math.absoluteValue


class ResultsFragment : Fragment(), View.OnTouchListener, GestureDetector.OnGestureListener {

    private var _binding: FragmentResultsBinding? = null

    private lateinit var myActivity: MainActivity
    private lateinit var autocompleteTV: AutoCompleteTextView
    private var isSuccess = false
    private var resultOptions: Array<NutritionResult> = arrayOf()
    private var selectedId = 0

    data class NutritionResult(
        val calories: Double,
        val proteins: Double,
        val zsir: Double,
        val rost: Double,
        val szenhidrat: Double
    )

    private val binding get() = _binding!!
    private var allCalories = 0
    private var allProteins = 0.0
    private var allZsir = 0.0
    private var allRost = 0.0
    private var allSzenhidrat = 0.0
    private val goalDiffMaxPercentage = 0.05
    private lateinit var gestureDetector: GestureDetector

    private fun getColoredSpanned(text: String, color: String): String {
        return "<b><font color=$color>$text</font></b>"
    }

    private fun getColoredSpannedLittle(text: String, color: String): String {
        return "<small><font color=$color>$text</small>"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentResultsBinding.inflate(inflater, container, false)

        myActivity = (activity as MainActivity?)!!
        val mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val root: View = binding.root
        val resultBtn: Button = binding.resultBtn
        val saveResultBtn: Button = binding.saveResultBtn
        val textView: TextView = binding.TextView
        textView.setOnTouchListener(this)
        val gifView: ImageView = binding.gifView
        gifView.setOnTouchListener(this)
        val darkener: LinearLayout = binding.darkener
        darkener.setOnTouchListener(this)
        val resultsLayout: LinearLayout = binding.resultsLayout

        // Initializing the gesture detector
        gestureDetector = GestureDetector(this)

        val foodsResults = ArrayList<String>()


        autocompleteTV  = binding.autoCompleteTextView
        context?.let {
            val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, foodsResults)
            autocompleteTV.setAdapter(arrayAdapter)
        }

        autocompleteTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id->
                saveResultBtn.visibility = View.VISIBLE
                selectedId = id.toInt()
            }

        when (myActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                darkener.alpha = 0.65F
            }
            Configuration.UI_MODE_NIGHT_NO -> {

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        myActivity.setMacros(-1, -1.0, -1.0, -1.0, -1.0)

        fun calculateNutrition(){
            allCalories = 0
            allProteins = 0.0
            allZsir = 0.0
            allRost = 0.0
            allSzenhidrat = 0.0

            for (selectedFoodPos in mainViewModel.clickedPosListBreakfast) {
                allCalories += myActivity.BreakfastList[selectedFoodPos].calories * myActivity.BreakfastList[selectedFoodPos].count
                allProteins += myActivity.BreakfastList[selectedFoodPos].proteins * myActivity.BreakfastList[selectedFoodPos].count
                allZsir += myActivity.BreakfastList[selectedFoodPos].zsir * myActivity.BreakfastList[selectedFoodPos].count
                allRost += myActivity.BreakfastList[selectedFoodPos].rost * myActivity.BreakfastList[selectedFoodPos].count
                allSzenhidrat += myActivity.BreakfastList[selectedFoodPos].szenhidrat * myActivity.BreakfastList[selectedFoodPos].count
            }

            for (selectedFoodPos in mainViewModel.clickedPosListLunch) {
                allCalories += myActivity.LunchList[selectedFoodPos].calories * myActivity.LunchList[selectedFoodPos].count
                allProteins += myActivity.LunchList[selectedFoodPos].proteins * myActivity.LunchList[selectedFoodPos].count
                allZsir += myActivity.LunchList[selectedFoodPos].zsir * myActivity.LunchList[selectedFoodPos].count
                allRost += myActivity.LunchList[selectedFoodPos].rost * myActivity.LunchList[selectedFoodPos].count
                allSzenhidrat += myActivity.LunchList[selectedFoodPos].szenhidrat * myActivity.LunchList[selectedFoodPos].count
            }

            for (selectedFoodPos in mainViewModel.clickedPosListSnacks) {
                allCalories += myActivity.SnacksList[selectedFoodPos].calories * myActivity.SnacksList[selectedFoodPos].count
                allProteins += myActivity.SnacksList[selectedFoodPos].proteins * myActivity.SnacksList[selectedFoodPos].count
                allZsir += myActivity.SnacksList[selectedFoodPos].zsir * myActivity.SnacksList[selectedFoodPos].count
                allRost += myActivity.SnacksList[selectedFoodPos].rost * myActivity.SnacksList[selectedFoodPos].count
                allSzenhidrat += myActivity.SnacksList[selectedFoodPos].szenhidrat * myActivity.SnacksList[selectedFoodPos].count
            }
        }

        fun calculateDiffPercentage(currentFoodCalories: Double, currentFoodProteins: Double, currentFoodZsir: Double, currentFoodRost: Double, currentFoodSzenhidrat: Double) : Double{
            val possibleCalorieDifference = ((myActivity.calorieGoal - allCalories - currentFoodCalories).absoluteValue / myActivity.calorieGoal)
            val possibleProteinDifference = ((myActivity.proteinGoal - allProteins - currentFoodProteins).absoluteValue / myActivity.proteinGoal)
            val possibleZsirDifference = ((myActivity.zsirGoal - allZsir - currentFoodZsir).absoluteValue / myActivity.zsirGoal)
            val possibleRostDifference = ((myActivity.rostGoal - allRost - currentFoodRost).absoluteValue / myActivity.rostGoal)
            val possibleSzenhidratDifference = ((myActivity.szenhidratGoal - allSzenhidrat - currentFoodSzenhidrat).absoluteValue / myActivity.szenhidratGoal)

            return if(myActivity.isCountingCalories){
                (possibleCalorieDifference + possibleProteinDifference + possibleZsirDifference + possibleRostDifference + possibleSzenhidratDifference)
            }else{
                (possibleProteinDifference + possibleZsirDifference + possibleRostDifference + possibleSzenhidratDifference)
            }
        }

        fun getDinner() :String{
            calculateNutrition()

            var textToBeDisplayed = ""//A következők közül válassz:\n\n"

            val allFoodsList: MutableList<Food> = mutableListOf()


            val TrimmedBreakfastList = myActivity.BreakfastList

            for(breakfast in TrimmedBreakfastList) {
                if (breakfast.count != 1){
                    breakfast.name = breakfast.name.substringBefore(delimiter = " (${breakfast.count}db)", missingDelimiterValue = breakfast.name)
                    breakfast.count = 1
                }
            }

            val TrimmedLunchList = myActivity.LunchList

            for(lunch in TrimmedLunchList) {
                if (lunch.count != 1){
                    lunch.name = lunch.name.substringBefore(delimiter = " (${lunch.count}db)", missingDelimiterValue = lunch.name)
                    lunch.count = 1
                }
            }

            val TrimmedSnacksList = myActivity.LunchList

            for(snack in TrimmedSnacksList) {
                if (snack.count != 1){
                    snack.name = snack.name.substringBefore(delimiter = " (${snack.count}db)", missingDelimiterValue = snack.name)
                    snack.count = 1
                }
            }

            allFoodsList.addAll(TrimmedBreakfastList)
            allFoodsList.addAll(TrimmedLunchList)
            allFoodsList.addAll(TrimmedSnacksList)
            allFoodsList.add(Food("Semmi",0,0.0,0.0,0.0,0.0))
            resultOptions = arrayOf(NutritionResult(0.0,0.0,0.0,0.0,0.0), NutritionResult(0.0,0.0,0.0,0.0,0.0), NutritionResult(0.0,0.0,0.0,0.0,0.0), NutritionResult(0.0,0.0,0.0,0.0,0.0))

            var first = 2.0
            var firstCal = 0.0
            var firstPro = 0.0
            var firstZsir = 0.0
            var firstRost = 0.0
            var firstSzenhidrat = 0.0
            var firstName = ""
            var second = 2.0
            var secondCal = 0.0
            var secondPro = 0.0
            var secondZsir = 0.0
            var secondRost = 0.0
            var secondSzenhidrat = 0.0
            var secondName = ""
            var third = 2.0
            var thirdCal = 0.0
            var thirdPro = 0.0
            var thirdZsir = 0.0
            var thirdRost = 0.0
            var thirdSzenhidrat = 0.0
            var thirdName = ""

            foodsResults.plusElement(firstName)

            for (food in allFoodsList){
                val current = calculateDiffPercentage(food.calories.toDouble(), food.proteins, food.zsir, food.rost, food.szenhidrat)
                val currentName = food.name

                if (first > current) {
                    third = second
                    thirdName = secondName
                    thirdCal = secondCal
                    thirdPro = secondPro
                    thirdZsir = secondZsir
                    thirdRost = secondRost
                    thirdSzenhidrat = secondSzenhidrat

                    second = first
                    secondName = firstName
                    secondCal = firstCal
                    secondPro = firstPro
                    secondZsir = firstZsir
                    secondRost = firstRost
                    secondSzenhidrat = firstSzenhidrat

                    first = current
                    firstName = currentName
                    firstCal = food.calories.toDouble()
                    firstPro = food.proteins
                    firstZsir = food.zsir
                    firstRost = food.rost
                    firstSzenhidrat = food.szenhidrat

                } else if (second > current) {
                    third = second
                    thirdName = secondName
                    thirdCal = secondCal
                    thirdPro = secondPro
                    thirdZsir = secondZsir
                    thirdRost = secondRost
                    thirdSzenhidrat = secondSzenhidrat

                    second = current
                    secondName = currentName
                    secondCal = food.calories.toDouble()
                    secondPro = food.proteins
                    secondZsir = food.zsir
                    secondRost = food.rost
                    secondSzenhidrat = food.szenhidrat

                } else if (third > current) {
                    third = current
                    thirdName = currentName
                    thirdCal = food.calories.toDouble()
                    thirdPro = food.proteins
                    thirdZsir = food.zsir
                    thirdRost = food.rost
                    thirdSzenhidrat = food.szenhidrat
                }
            }
            foodsResults.add("Valami mást")
            foodsResults.add(firstName)

            resultOptions[0] = NutritionResult(allCalories.toDouble(), allProteins, allZsir, allRost, allSzenhidrat)
            resultOptions[1] = NutritionResult(allCalories.toDouble()+firstCal, allProteins+firstPro, allZsir+firstZsir, allRost+firstRost, allSzenhidrat+firstSzenhidrat)
            resultOptions[2] = NutritionResult(allCalories.toDouble()+secondCal, allProteins+secondPro, allZsir+secondZsir, allRost+secondRost, allSzenhidrat+secondSzenhidrat)
            resultOptions[3] = NutritionResult(allCalories.toDouble()+thirdCal, allProteins+thirdPro, allZsir+thirdZsir, allRost+thirdRost, allSzenhidrat+thirdSzenhidrat)

            if(first < 2 * goalDiffMaxPercentage){
                textToBeDisplayed = "Sikerülhet elérni a célodat, mert már " + getColoredSpanned("$allCalories/${myActivity.calorieGoal}", "#d1e659") +
                        " kalóriát, " + getColoredSpanned("$allProteins/${myActivity.proteinGoal}", "#d1e659") +
                        " fehérjét, " + getColoredSpanned("$allZsir/${myActivity.zsirGoal}", "#d1e659") +
                        " zsírt, " + getColoredSpanned("$allRost/${myActivity.rostGoal}", "#d1e659") +
                        " rostot és " + getColoredSpanned("$allSzenhidrat/${myActivity.szenhidratGoal}", "#d1e659") +
                        " szénhidrátot vittél be!<br/><br/>Ehhez a következő(k) közül válassz:<br/><br/>"
                textToBeDisplayed += getColoredSpanned("1. $firstName ", "#42a543") +
                        getColoredSpannedLittle("(${allCalories.toDouble()+firstCal}/${myActivity.calorieGoal}, ${allProteins+firstPro}/${myActivity.proteinGoal}, ${allZsir+firstZsir}/${myActivity.zsirGoal}, ${allRost+firstRost}/${myActivity.rostGoal}, ${allSzenhidrat+firstSzenhidrat}/${myActivity.szenhidratGoal})<br/>", "#d1e659")
                if(second < 2 * goalDiffMaxPercentage){
                    foodsResults.add(secondName)
                    textToBeDisplayed += getColoredSpanned("2. $secondName ", "#42a543") +
                            getColoredSpannedLittle("(${allCalories.toDouble()+secondCal}/${myActivity.calorieGoal}, ${allProteins+secondPro}/${myActivity.proteinGoal}, ${allZsir+secondZsir}/${myActivity.zsirGoal}, ${allRost+secondRost}/${myActivity.rostGoal}, ${allSzenhidrat+secondSzenhidrat}/${myActivity.szenhidratGoal})<br/>", "#d1e659")
                    if(third < 2 * goalDiffMaxPercentage){
                        foodsResults.add(thirdName)
                        textToBeDisplayed += getColoredSpanned("3. $thirdName ", "#42a543") +
                                getColoredSpannedLittle("(${allCalories.toDouble()+thirdCal}/${myActivity.calorieGoal}, ${allProteins+thirdPro}/${myActivity.proteinGoal}, ${allZsir+thirdZsir}/${myActivity.zsirGoal}, ${allRost+thirdRost}/${myActivity.rostGoal}, ${allSzenhidrat+thirdSzenhidrat}/${myActivity.szenhidratGoal})", "#d1e659")
                    }
                }
                Glide.with(this).load(R.drawable.bravocado).into(gifView)
                isSuccess = true
            }else{
                textToBeDisplayed += "Sajnos ma már nem tudod elérni a célodat, mert " + getColoredSpanned("$allCalories/${myActivity.calorieGoal}", "#dd1324") +
                        " kalóriát, " + getColoredSpanned("$allProteins/${myActivity.proteinGoal}", "#dd1324") +
                        " fehérjét, " + getColoredSpanned("$allZsir/${myActivity.zsirGoal}", "#dd1324") +
                        " zsírt, " + getColoredSpanned("$allRost/${myActivity.rostGoal}", "#dd1324") +
                        " rostot és " + getColoredSpanned("$allSzenhidrat/${myActivity.szenhidratGoal}", "#dd1324") +
                        " szénhidrátot vittél be." +
                        "<br/><br/>Mindenesetre a legjobb választás a(z): " + getColoredSpanned(
                    firstName, "#42a543") + " lenne. <br/><br/>Ezzel " +
                        getColoredSpanned("${allCalories.toDouble()+firstCal}/${myActivity.calorieGoal}", "#d1e659") +
                        " kalóriát, " + getColoredSpanned("${allProteins+firstPro}/${myActivity.proteinGoal}", "#d1e659") +
                        " fehérjét, " + getColoredSpanned("${allZsir+firstZsir}/${myActivity.zsirGoal}", "#d1e659") +
                        " zsírt, " + getColoredSpanned("${allRost+firstRost}/${myActivity.rostGoal}", "#d1e659") +
                        " rostot és " + getColoredSpanned("${allSzenhidrat+firstSzenhidrat}/${myActivity.szenhidratGoal}", "#d1e659") +
                        " szénhidrátot fogsz bevinni."
                Glide.with(this).load(R.drawable.nahvocado).into(gifView)
                isSuccess = false
            }

            return textToBeDisplayed
        }

        resultBtn.setOnClickListener {
            textView.text = (Html.fromHtml(getDinner()))
            resultBtn.visibility = View.INVISIBLE
            resultsLayout.visibility = View.VISIBLE
        }

        saveResultBtn.setOnClickListener{
            Toast.makeText(
                context,
                "Elmentve!", Toast.LENGTH_SHORT).show()

            if(selectedId == 0) {
                isSuccess = false
            }
            val scoredCalories = "${resultOptions[selectedId].calories.toInt()} / ${myActivity.calorieGoal}"
            val scoredProteins = "${resultOptions[selectedId].proteins.toInt()} / ${myActivity.proteinGoal}"
            val scoredZsir = "${resultOptions[selectedId].zsir.toInt()} / ${myActivity.zsirGoal}"
            val scoredRost = "${resultOptions[selectedId].rost.toInt()} / ${myActivity.rostGoal}"
            val scoredSzenhidrat = "${resultOptions[selectedId].szenhidrat.toInt()} / ${myActivity.szenhidratGoal}"

            myActivity.saveResults(DayResult(myActivity.calendar.get(Calendar.YEAR), myActivity.calendar.get(Calendar.MONTH),
                myActivity.calendar.get(Calendar.DATE), scoredCalories, scoredProteins, scoredZsir, scoredRost, scoredSzenhidrat, isSuccess))

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onTouch(v: View?, e: MotionEvent?): Boolean {
        return e?.let { gestureDetector.onTouchEvent(it) } ?: false
    }

    // All the below methods are GestureDetector.OnGestureListener members
    // Except onFling, all must "return false" if Boolean return type
    // and "return" if no return type
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val myActivity = (activity as MainActivity?)!!
        return myActivity.onFling(e1, e2, velocityX, velocityY)
    }
}
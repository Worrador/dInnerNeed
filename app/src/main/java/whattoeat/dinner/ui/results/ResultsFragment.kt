package whattoeat.dinner.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.Food
import whattoeat.dinner.MainActivity
import whattoeat.dinner.databinding.FragmentResultsBinding
import whattoeat.dinner.ui.MainViewModel
import kotlin.math.absoluteValue
import whattoeat.dinner.R
import com.bumptech.glide.Glide
import whattoeat.dinner.hideKeyboard

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null

    lateinit var myActivity: MainActivity

    private val binding get() = _binding!!
    var allCalories = 0
    var allProteins = 0
    private val goalDiffMaxPercentage = 0.05

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
        val textView: TextView = binding.TextView
        val gifView: ImageView = binding.gifView

        fun calculateNutrition(){
            allCalories = 0
            allProteins = 0

            for (selectedFoodPos in mainViewModel.clickedPosListBreakfast)
                allCalories += myActivity.BreakfastList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListBreakfast)
                allProteins += myActivity.BreakfastList[selectedFoodPos].proteins

            for (selectedFoodPos in mainViewModel.clickedPosListLunch)
                allCalories += myActivity.LunchList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListLunch)
                allProteins += myActivity.LunchList[selectedFoodPos].proteins

            for (selectedFoodPos in mainViewModel.clickedPosListSnack)
                allCalories += myActivity.SnackList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListSnack)
                allProteins += myActivity.SnackList[selectedFoodPos].proteins
        }

        fun calculateDiffPercentage(currentFoodCalories: Double, currentFoodProteins: Double) : Double{
            var possibleCalorieDifference = ((myActivity.calorieGoal - allCalories - currentFoodCalories).absoluteValue / myActivity.calorieGoal)
            var possibleProteinDifference = ((myActivity.proteinGoal - allProteins - currentFoodProteins).absoluteValue / myActivity.proteinGoal)

            return (possibleCalorieDifference + possibleProteinDifference)
        }

        fun getDinner() :String{
            calculateNutrition()

            var textToBeDisplayed = ""//A következők közül válassz:\n\n"

            var allFoodsList: MutableList<Food> = mutableListOf<Food>()
            allFoodsList.addAll(myActivity.BreakfastList)
            allFoodsList.addAll(myActivity.LunchList)
            allFoodsList.addAll(myActivity.SnackList)
            allFoodsList.add(Food("Semmi",0,0))

            var first = 2.0
            var firstName = ""
            var second = 2.0
            var secondName = ""
            var third = 2.0
            var thirdName = ""

            for (food in allFoodsList){
                val current = calculateDiffPercentage(food.calories.toDouble(), food.proteins.toDouble())
                val currentName = food.name

                if (first > current) {
                    third = second
                    thirdName = secondName
                    second = first
                    secondName = firstName
                    first = current
                    firstName = currentName

                } else if (second > current) {
                    third = second
                    thirdName = secondName
                    second = current
                    secondName = currentName

                } else if (third > current) {
                    third = current
                    thirdName = currentName
                }
            }
            if(first < 2 * goalDiffMaxPercentage){
                textToBeDisplayed = "Sikerülhet elérni a célodat!\nEhhez a következő(k) közül válassz:\n\n"
                textToBeDisplayed += "1. $firstName\n"
                if(second < 2 * goalDiffMaxPercentage){
                    textToBeDisplayed += "2. $secondName\n"
                    if(third < 2 * goalDiffMaxPercentage){
                        textToBeDisplayed += "3. $thirdName"
                    }
                }
                Glide.with(this).load(R.drawable.bravocado).into(gifView)
            }else{
                textToBeDisplayed += "Sajnos ma már nem tudod elérni a célodat.\nMindenesetre a legjobb választás:\n\n$firstName"
                Glide.with(this).load(R.drawable.nahvocado).into(gifView)
            }
            
            return textToBeDisplayed
        }

        resultBtn.setOnClickListener {
            textView.text = getDinner()
            resultBtn.visibility = View.INVISIBLE
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
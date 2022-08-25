package whattoeat.dinner.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.MainActivity
import whattoeat.dinner.databinding.FragmentResultsBinding
import whattoeat.dinner.ui.MainViewModel

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null

    lateinit var myActivity: MainActivity

    private val binding get() = _binding!!
    private var allCalories = 0
    private var allProteins = 0

    private val calorieGoal = 1800
    private val proteinGoal = 50

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

        fun calculateNutrition(){
            allCalories = 0
            allProteins = 0

            for (selectedFoodPos in mainViewModel.clickedPosListBreakfast)
                allCalories += myActivity.BreakfastList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListBreakfast)
                allCalories += myActivity.BreakfastList[selectedFoodPos].proteins

            for (selectedFoodPos in mainViewModel.clickedPosListLunch)
                allCalories += myActivity.LunchList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListLunch)
                allCalories += myActivity.LunchList[selectedFoodPos].proteins

            for (selectedFoodPos in mainViewModel.clickedPosListSnack)
                allCalories += myActivity.SnackList[selectedFoodPos].calories
            for (selectedFoodPos in mainViewModel.clickedPosListSnack)
                allCalories += myActivity.SnackList[selectedFoodPos].proteins
        }

        fun getDinner() :String{
            calculateNutrition()

            var textToBeDisplayed = "A következők közül válassz:\n"
            for (food in myActivity.LunchList){
                if ((food.calories + allCalories) >= calorieGoal){
                    textToBeDisplayed += food.name + "\n"
                }
            }

            return textToBeDisplayed
        }

        resultBtn.setOnClickListener {
            textView.text = getDinner()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
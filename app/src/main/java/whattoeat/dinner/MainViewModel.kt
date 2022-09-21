package whattoeat.dinner.ui
import androidx.lifecycle.ViewModel
import whattoeat.dinner.ui.Meals.Food


class MainViewModel : ViewModel() {
    var clickedPosListBreakfast: ArrayList<Int> = ArrayList()
    var clickedPosListLunch: ArrayList<Int> = ArrayList()
    var clickedPosListSnacks: ArrayList<Int> = ArrayList()

    fun setMultipleListView(FoodList: MutableList<Food>): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()
        for (element in FoodList)
            arrayList.add(element.name)
        return arrayList
    }
}
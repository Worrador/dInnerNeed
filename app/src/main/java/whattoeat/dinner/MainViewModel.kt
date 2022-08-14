package whattoeat.dinner.ui
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    var clickedPosListBreakfast: ArrayList<Int> = ArrayList()
    var clickedPosListLunch: ArrayList<Int> = ArrayList()
    var clickedPosListSnack: ArrayList<Int> = ArrayList()


    fun setMultipleListView(FoodList: MutableSet<String>): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()
        for (element in FoodList)
            arrayList.add(element)
        return arrayList
    }
}
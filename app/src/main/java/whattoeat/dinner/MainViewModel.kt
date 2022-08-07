package whattoeat.dinner.ui
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    var positionClickedArrayList: ArrayList<Int> = ArrayList()
    fun setMultipleListView(BreakfastList: MutableSet<String>): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()
        for (element in BreakfastList)
            arrayList.add(element)
        return arrayList
    }
}
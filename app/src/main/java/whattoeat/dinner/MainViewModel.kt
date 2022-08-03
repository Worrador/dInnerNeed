package whattoeat.dinner.ui

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import whattoeat.dinner.ui.breakfast.BreakfastList

class MainViewModel : ViewModel() {
    var positionClickedArrayList: ArrayList<Int> = ArrayList()
    var BreakfastList: MutableSet<String> = mutableSetOf<String>()

    fun setMultipleListView(): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()

        for (element in BreakfastList)
            arrayList.add(element)

        return arrayList
    }
}

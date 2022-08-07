package whattoeat.dinner.ui

import android.app.Application
import androidx.lifecycle.*

class MyViewModelFactory(private val mParam: MutableSet<String>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(mParam) as T
    }
}

class MainViewModel(private val MyBreakfastList: MutableSet<String>) : ViewModel() {

    var positionClickedArrayList: ArrayList<Int> = ArrayList()
    var BreakfastList2: MutableSet<String> = mutableSetOf<String>()

    fun setMultipleListView(BreakfastList: MutableSet<String> ): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()

        for (element in BreakfastList)
            arrayList.add(element)

        return arrayList
    }

    init {
        BreakfastList2 = MyBreakfastList
    }
}

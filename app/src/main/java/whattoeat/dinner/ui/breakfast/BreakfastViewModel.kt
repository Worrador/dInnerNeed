package whattoeat.dinner.ui.breakfast

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BreakfastViewModel : ViewModel() {

    companion object var positionClickedArrayList: ArrayList<Int> = ArrayList()

    fun setMultipleListView(): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()

        arrayList.add("Avokádós kenyér")
        arrayList.add("Melegszendvics")
        arrayList.add("Kása")
        arrayList.add("Müzli")

        return arrayList
    }
}

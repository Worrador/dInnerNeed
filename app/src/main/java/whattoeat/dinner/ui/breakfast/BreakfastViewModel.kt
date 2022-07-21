package whattoeat.dinner.ui.breakfast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BreakfastViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is breakfast Fragment"
    }
    val text: LiveData<String> = _text
}
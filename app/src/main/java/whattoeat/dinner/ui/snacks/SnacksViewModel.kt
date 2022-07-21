package whattoeat.dinner.ui.snacks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SnacksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is snacks Fragment"
    }
    val text: LiveData<String> = _text
}
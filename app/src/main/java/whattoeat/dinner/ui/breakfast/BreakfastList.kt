package whattoeat.dinner.ui.breakfast

import android.content.SharedPreferences

class BreakfastList (sharedPref : SharedPreferences){
    private val PREFERENCE_FILE_KEY = "AppPreference"
    private val KEY_USERNAME= "prefUserNameKey"

    val arrayList: ArrayList<String> = ArrayList()
}
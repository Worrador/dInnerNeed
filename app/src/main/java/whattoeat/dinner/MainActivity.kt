package whattoeat.dinner

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import whattoeat.dinner.databinding.ActivityMainBinding
import whattoeat.dinner.ui.MainViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var mainViewModel : MainViewModel
    var BreakfastList: List<Food> = mutableListOf<Food>()
    var LunchList: List<Food> = mutableListOf<Food>()
    var SnackList: List<Food> = mutableListOf<Food>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLists()

        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_breakfast, R.id.navigation_lunch, R.id.navigation_snacks, R.id.navigation_results
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())

            // When the screen is swiped up at the bottom
            // of the application, the navigationBar shall
            // appear for some time
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun getLists(){
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson();

        BreakfastList = gson.fromJson(sh.getString("breakfast", BreakfastList), mutableListOf<Food>)
        LunchList = gson.fromJson(sh.getString("lunch", LunchList), mutableListOf<Food>)
        SnackList = gson.fromJson(sh.getString("snack", SnackList), mutableListOf<Food>)
    }

    override fun onResume() {
        super.onResume()
        getLists()
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getPreferences(Context.MODE_APPEND)
        val gson = Gson();
        val myEdit = sharedPreferences.edit()

        myEdit.remove("breakfast")
        myEdit.commit()
        myEdit.putString("breakfast", gson.toJson(BreakfastList))
        myEdit.commit()

        myEdit.remove("lunch")
        myEdit.commit()
        myEdit.putString("lunch", gson.toJson(LunchList))
        myEdit.commit()

        myEdit.remove("snack")
        myEdit.commit()
        myEdit.putString("snack", gson.toJson(SnackList))
        myEdit.commit()
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
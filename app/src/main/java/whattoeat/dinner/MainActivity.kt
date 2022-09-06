package whattoeat.dinner

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.LightingColorFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import whattoeat.dinner.databinding.ActivityMainBinding
import whattoeat.dinner.ui.MainViewModel
import java.lang.reflect.Type
import java.util.*
import whattoeat.dinner.R as R2

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var mainViewModel : MainViewModel
    var BreakfastList: MutableList<Food> = mutableListOf<Food>()
    var LunchList: MutableList<Food> = mutableListOf<Food>()
    var SnackList: MutableList<Food> = mutableListOf<Food>()
    var isMenuVisible = false


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        } else {
            // When PopupMenu appears, the current Activity looses the focus
            setFlagsOnThePeekView() // Hijack to the current peek view, apply the Flags on it
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun setFlagsOnThePeekView() {
        try {
            val wmgClass = Class.forName("android.view.WindowManagerGlobal")
            val wmgInstance = wmgClass.getMethod("getInstance").invoke(null)
            val viewsField = wmgClass.getDeclaredField("mViews")
            viewsField.isAccessible = true

            val views = viewsField.get(wmgInstance) as ArrayList<View>
            // When the popup appears, its decorView is the peek of the stack aka last item
            views.last().apply {
                hideSystemUI()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLists()

        hideSystemUI()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // assigning ID of the toolbar to a variable
        val toolbar = findViewById<View>(R2.id.toolbar) as Toolbar

        val goalMenu: LinearLayout = binding.linLayoutInner1

        val historymenu: LinearLayout = binding.linLayoutInner2

        goalMenu.setOnClickListener(){
            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.dialog_fragment, null)

            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }

        // using toolbar as ActionBar
        setSupportActionBar(toolbar)
        val imageView: ImageButton = binding.avocadoIconMenu

        if(resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES){
            val lcf = LightingColorFilter(0xFF808080.toInt(), 0x00000000)
            imageView.colorFilter = lcf
        }

        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R2.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R2.id.navigation_breakfast, R2.id.navigation_lunch, R2.id.navigation_snacks, R2.id.navigation_results
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun toggleMenu(view: View) {

        val menu: LinearLayout = binding.linLayout

        val divider: LinearLayout = binding.linLayoutDivider

        //Load animation
        val slide_down = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_down
        )

        val slide_up = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_up
        )

        if(isMenuVisible){
            menu.startAnimation(slide_up)
            menu.visibility = View.INVISIBLE
        }else{
            menu.startAnimation(slide_down)
            menu.visibility = View.VISIBLE
        }


        //Load animation
        val slide_down_divider = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_down_divider
        )

        val slide_up_divider = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_up_divider
        )


        if(isMenuVisible){
            divider.startAnimation(slide_up_divider)
            divider.visibility = View.INVISIBLE
        }else{
            divider.startAnimation(slide_down_divider)
            divider.visibility = View.VISIBLE
        }

        isMenuVisible = isMenuVisible.not()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun setMacros(calories: Int, proteins: Int){
        val calTextView: TextView = binding.textViewCaloriesCounter
        val proTextView: TextView = binding.textViewProteinsCounter
        calTextView.text = "cal: $calories"
        proTextView.text = "pro: $proteins"
    }

    fun getLists(){
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson();
        val typeOfObjectsList: Type = object : TypeToken<List<Food?>?>() {}.type

        BreakfastList = gson.fromJson(sh.getString("breakfastList", gson.toJson(BreakfastList).toString()), typeOfObjectsList)
        LunchList = gson.fromJson(sh.getString("lunchList", gson.toJson(LunchList).toString()), typeOfObjectsList)
        SnackList = gson.fromJson(sh.getString("snackList", gson.toJson(SnackList)), typeOfObjectsList)
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

        myEdit.remove("breakfastList")
        myEdit.commit()
        myEdit.putString("breakfastList", gson.toJson(BreakfastList))
        myEdit.commit()

        myEdit.remove("lunchList")
        myEdit.commit()
        myEdit.putString("lunchList", gson.toJson(LunchList))
        myEdit.commit()

        myEdit.remove("snackList")
        myEdit.commit()
        myEdit.putString("snackList", gson.toJson(SnackList))
        myEdit.commit()
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
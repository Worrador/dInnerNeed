package whattoeat.dinner

import android.R
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.LightingColorFilter
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_MULTIPLE
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
    var SnacksList: MutableList<Food> = mutableListOf<Food>()
    var isMenuVisible = false
    var calorieGoal = 1800
    var proteinGoal = 50

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
        val historyMenu: LinearLayout = binding.linLayoutInner2

        fun PopupWindow.dimBehind() {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val p = container.layoutParams as WindowManager.LayoutParams
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            p.dimAmount = 0.3f
            wm.updateViewLayout(container, p)
        }

        goalMenu.setOnClickListener{
            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.goal_dialog_fragment, null)

            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.isOutsideTouchable = false;
            popupWindow.animationStyle = R2.style.Animation;

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, -100)
            popupWindow.dimBehind()


            val cancelBtn = popupView.findViewById<View>(R2.id.cancelBtn) as FloatingActionButton
            val checkBtn = popupView.findViewById<View>(R2.id.checkBtn) as FloatingActionButton
            val caloriesEditText = popupView.findViewById<View>(R2.id.editTextNumber1) as EditText
            val proteinsEditText = popupView.findViewById<View>(R2.id.editTextNumber2) as EditText

            caloriesEditText.hint = "$calorieGoal(kcal)"
            proteinsEditText.hint = "$proteinGoal(g)"

            checkBtn.setOnClickListener{
                calorieGoal = caloriesEditText.text.toString().toInt()
                proteinGoal = proteinsEditText.text.toString().toInt()
                popupWindow.dismiss()
            }
            cancelBtn.setOnClickListener{
                popupWindow.dismiss();
            }
        }

        historyMenu.setOnClickListener{
            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.calendar_dialog_fragment, null)

            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.isOutsideTouchable = false;
            popupWindow.animationStyle = R2.style.Animation;

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, -100)

            popupWindow.dimBehind()

            val cancelBtn = popupView.findViewById<View>(R2.id.cancelBtn) as FloatingActionButton
            val checkBtn = popupView.findViewById<View>(R2.id.checkBtn) as FloatingActionButton
            val calView = popupView.findViewById<View>(R2.id.calendarView) as MaterialCalendarView

            calView.selectionMode = SELECTION_MODE_MULTIPLE

            val c = Calendar.getInstance().time
            calView.setDateSelected(c, true)

            checkBtn.setOnClickListener{
                popupWindow.dismiss()
            }
            cancelBtn.setOnClickListener{
                popupWindow.dismiss();
            }
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
        if((calories >= 0) && (proteins >= 0)) {
            calTextView.text = "cal: $calories"
            proTextView.text = "pro: $proteins"
        }else
        {
            calTextView.text = ""
            proTextView.text = ""
        }
    }

    fun getLists(){
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson();
        val typeOfObjectsList: Type = object : TypeToken<List<Food?>?>() {}.type

        BreakfastList = gson.fromJson(sh.getString("breakfastList", gson.toJson(BreakfastList).toString()), typeOfObjectsList)
        LunchList = gson.fromJson(sh.getString("lunchList", gson.toJson(LunchList).toString()), typeOfObjectsList)
        SnacksList = gson.fromJson(sh.getString("snackList", gson.toJson(SnacksList)), typeOfObjectsList)

        calorieGoal = sh.getInt("calorieGoal", calorieGoal)
        proteinGoal = sh.getInt("proteinGoal", proteinGoal)
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
        myEdit.putString("snackList", gson.toJson(SnacksList))
        myEdit.commit()

        myEdit.remove("calorieGoal")
        myEdit.commit()
        myEdit.putInt("calorieGoal", calorieGoal)
        myEdit.commit()

        myEdit.remove("proteinGoal")
        myEdit.commit()
        myEdit.putInt("proteinGoal", proteinGoal)
        myEdit.commit()

    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
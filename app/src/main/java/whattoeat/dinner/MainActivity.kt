package whattoeat.dinner

import android.R
import android.R.attr.data
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.LightingColorFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import whattoeat.dinner.databinding.ActivityMainBinding
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.ui.Meals.Food
import whattoeat.dinner.ui.Results.DayResult
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.*
import kotlin.math.abs
import whattoeat.dinner.R as R2


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    lateinit var navView: BottomNavigationView
    lateinit var navController: NavController
    val calendar: Calendar = Calendar.getInstance()
    var events: MutableList<EventDay> = ArrayList()
    var dayResults: MutableList<DayResult> = ArrayList()
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    lateinit var mainViewModel : MainViewModel
    var BreakfastList: MutableList<Food> = mutableListOf<Food>()
    var LunchList: MutableList<Food> = mutableListOf<Food>()
    var SnacksList: MutableList<Food> = mutableListOf<Food>()
    var isMenuVisible = false
    var calorieGoal = 1800
    var proteinGoal = 50
    var currentFragmentidx = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLists()

        hideSystemUI()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing the gesture detector
        gestureDetector = GestureDetector(this)

        // assigning ID of the toolbar to a variable
        val toolbar = findViewById<View>(R2.id.toolbar) as Toolbar
        val goalMenu: LinearLayout = binding.linLayoutInner1
        val historyMenu: LinearLayout = binding.linLayoutInner2
        navView = binding.navView
        var navbarMock: LinearLayout = findViewById(R2.id.navbar_mock)
        navbarMock.visibility = View.VISIBLE
        val params: ViewGroup.LayoutParams = navbarMock.layoutParams
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        params.height = resources.getDimensionPixelSize(resourceId)
        navbarMock.layoutParams = params

        fun PopupWindow.dimBehind() {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val p = container.layoutParams as WindowManager.LayoutParams
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            p.dimAmount = 0.5f
            wm.updateViewLayout(container, p)
        }

        goalMenu.setOnClickListener{
            navView.visibility = View.INVISIBLE

            val slide_down = AnimationUtils.loadAnimation(
                applicationContext,
                R2.anim.slide_down_navbar
            )

            val slide_up = AnimationUtils.loadAnimation(
                applicationContext,
                R2.anim.slide_up_navbar
            )

            navbarMock.startAnimation(slide_up)
            navView.startAnimation(slide_down)


            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.goal_dialog_fragment, null)

            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.isOutsideTouchable = false
            popupWindow.animationStyle = R2.style.Animation

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, -150)
            popupWindow.dimBehind()

            val checkBtn = popupView.findViewById<View>(R2.id.checkBtn) as Button
            val caloriesEditText = popupView.findViewById<View>(R2.id.editTextNumber1) as EditText
            val proteinsEditText = popupView.findViewById<View>(R2.id.editTextNumber2) as EditText

            caloriesEditText.hint = "$calorieGoal(kcal)"
            proteinsEditText.hint = "$proteinGoal(g)"

            popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {

                navView.visibility = View.VISIBLE
                navbarMock.startAnimation(slide_down)
                navView.startAnimation(slide_up)
                popupWindow.dismiss()
            })

            checkBtn.setOnClickListener{
                if((caloriesEditText.text != null) && (proteinsEditText.text != null)){
                    try {
                        calorieGoal = caloriesEditText.text.toString().toInt()
                        proteinGoal = proteinsEditText.text.toString().toInt()
                    }catch (e: Exception)
                    {}
                }

                popupWindow.dismiss()
            }

        }

        historyMenu.setOnClickListener{
            navView.visibility = View.INVISIBLE

            val slide_down = AnimationUtils.loadAnimation(
                applicationContext,
                R2.anim.slide_down_navbar
            )

            val slide_up = AnimationUtils.loadAnimation(
                applicationContext,
                R2.anim.slide_up_navbar
            )
            navbarMock.startAnimation(slide_up)
            navView.startAnimation(slide_down)

            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.calendar_dialog_fragment, null)

            // create the popup window
            val width = 850
            val height = 1700
            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.isOutsideTouchable = false;
            popupWindow.animationStyle = R2.style.Animation;

            val calendarView = popupView.findViewById<View>(R2.id.calendarView) as CalendarView



            popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
                popupWindow.dismiss()
                navView.visibility = View.VISIBLE
                navView.startAnimation(slide_up)
                popupWindow.dismiss()
            })

            var calendar1: Calendar = Calendar.getInstance()
            calendar1.set(2022, 8, 15)
            events.add(EventDay(calendar1, R2.drawable.ic_cancel_red))
            calendarView.setEvents(events)



            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, -100)

            popupWindow.dimBehind()
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R2.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

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


        val typeOfResultList: Type = object : TypeToken<List<DayResult?>?>() {}.type
        dayResults = gson.fromJson(sh.getString("resultList", gson.toJson(dayResults).toString()), typeOfResultList)
        events.clear()
        var calendarTemp: Calendar = Calendar.getInstance()
        for (result in dayResults){
            calendarTemp.set(result.year, result.month.toInt(), result.day.toInt())
            if(result.isSuccess) {
                events.add(EventDay(calendarTemp, R2.drawable.ic_check_green))
            }else
            {
                events.add(EventDay(calendarTemp, R2.drawable.ic_cancel_red))
            }
        }


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

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveResults(newResult: DayResult){

        // Delete date from array if already present:
        for(result in dayResults){
            if (result.day == newResult.day){
                dayResults.remove(result)
                break
            }
        }

        // Save preferences:
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson();
        val myEdit = sh.edit()
        dayResults.add(newResult)

        myEdit.remove("resultList")
        myEdit.commit()
        myEdit.putString("resultList", gson.toJson(dayResults))
        myEdit.commit()

        events.clear()
        var calendarTemp: Calendar = Calendar.getInstance()

        for (result in dayResults){
            calendarTemp.set(result.year, result.month.toInt(), result.day.toInt())
            if(result.isSuccess) {
                events.add(EventDay(calendarTemp, R2.drawable.ic_check_green))
            } else {
                events.add(EventDay(calendarTemp, R2.drawable.ic_cancel_red))
            }
        }

        //settings.edit().putLong("firstDate", System.currentTimeMillis()).commit();
        //val firstDate = sh.getLong("firstDate", calendar.timeInMillis)
    }

    // Override this method to recognize touch event
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    // All the below methods are GestureDetector.OnGestureListener members
    // Except onFling, all must "return false" if Boolean return type
    // and "return" if no return type
    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        return
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {

                    if(navView.menu.getItem(0).isChecked)
                        currentFragmentidx = 0
                    else if(navView.menu.getItem(1).isChecked)
                        currentFragmentidx = 1
                    else if(navView.menu.getItem(2).isChecked)
                        currentFragmentidx = 2
                    else if(navView.menu.getItem(3).isChecked)
                        currentFragmentidx = 3

                    if (diffX > 0) {

                        if(currentFragmentidx > 0){
                            currentFragmentidx -= 1
                        }
                    }
                    else {
                        if(currentFragmentidx < 3){
                            currentFragmentidx += 1
                        }
                    }
                    val navItem: MenuItem  = navView.menu.getItem(currentFragmentidx)
                    navItem.onNavDestinationSelected(navController)
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
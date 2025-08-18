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
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.applandeo.materialcalendarview.EventDay
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import whattoeat.dinner.databinding.ActivityMainBinding
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.ui.Meals.Food
import whattoeat.dinner.ui.Results.DayResult
import java.lang.reflect.Type
import java.util.*
import kotlin.math.abs
import whattoeat.dinner.R as R2
import com.applandeo.materialcalendarview.CalendarView


class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector
    private var startingYear = 2022
    private var startingMonth = 0
    private var startingDay = 0
    private lateinit var navView: BottomNavigationView
    private lateinit var navController: NavController

    private lateinit var leftSide: ConstraintLayout
    private lateinit var rightSide: ConstraintLayout
    val calendar: Calendar = Calendar.getInstance()
    private val currentYear = calendar.get(Calendar.YEAR)
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentDay = calendar.get(Calendar.DATE)
    private var events: MutableList<EventDay> = ArrayList()
    private var dayResults: MutableList<DayResult> = ArrayList()
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    private lateinit var mainViewModel : MainViewModel
    var BreakfastList: MutableList<Food> = mutableListOf()
    var LunchList: MutableList<Food> = mutableListOf()
    var SnacksList: MutableList<Food> = mutableListOf()
    private var isMenuVisible = false
    var calorieGoal = 1800
    var isCountingCalories = true
    var proteinGoal = 50
    var zsirGoal = 65
    var rostGoal = 25
    var szenhidratGoal = 200
    private var currentFragmentidx = 0
    private lateinit var calTextView: TextView
    private lateinit var proTextView: TextView
    // Note: New nutrient fields will be added to layout later
    // zsirTextView = binding.editViewZsirCounter
    // rostTextView = binding.editViewRostCounter
    // szenhidratTextView = binding.editViewSzenhidratCounter

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLists()

        hideSystemUI()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calTextView = binding.editViewCaloriesCounter
        proTextView = binding.editViewProteinsCounter
        // Note: New nutrient fields will be added to layout later
        // zsirTextView = binding.editViewZsirCounter
        // rostTextView = binding.editViewRostCounter
        // szenhidratTextView = binding.editViewSzenhidratCounter
        leftSide = binding.leftSide
        rightSide = binding.rightSide

        // Initializing the gesture detector
        gestureDetector = GestureDetector(this)

        // assigning ID of the toolbar to a variable
        val toolbar = findViewById<View>(R2.id.toolbar) as Toolbar
        val goalMenu: LinearLayout = binding.linLayoutInner1
        val historyMenu: LinearLayout = binding.linLayoutInner2
        navView = binding.navView

        fun PopupWindow.dimBehind() {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val p = container.layoutParams as WindowManager.LayoutParams
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            p.dimAmount = 0.6f
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
            val zsirEditText = popupView.findViewById<View>(R2.id.editTextNumber3) as EditText
            val rostEditText = popupView.findViewById<View>(R2.id.editTextNumber4) as EditText
            val szenhidratEditText = popupView.findViewById<View>(R2.id.editTextNumber5) as EditText

            caloriesEditText.hint = "${calorieGoal}kcal"
            proteinsEditText.hint = "${proteinGoal}g"
            zsirEditText.hint = "${zsirGoal}g"
            rostEditText.hint = "${rostGoal}g"
            szenhidratEditText.hint = "${szenhidratGoal}g"

            popupWindow.setOnDismissListener{
                navView.visibility = View.VISIBLE
                navView.startAnimation(slide_up)
                toggleMenu(popupView)
                hideSystemUI()
            }

            checkBtn.setOnClickListener{
                if((caloriesEditText.text != null) && (proteinsEditText.text != null) && (zsirEditText.text != null) && (rostEditText.text != null) && (szenhidratEditText.text != null)){
                    try {
                        calorieGoal = caloriesEditText.text.toString().toInt()
                        if(calorieGoal == 0){
                            isCountingCalories = false
                        }else{
                            isCountingCalories = true
                        }
                        proteinGoal = proteinsEditText.text.toString().toInt()
                        zsirGoal = zsirEditText.text.toString().toInt()
                        rostGoal = rostEditText.text.toString().toInt()
                        szenhidratGoal = szenhidratEditText.text.toString().toInt()
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
            navView.startAnimation(slide_down)

            // inflate the layout of the popup window
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R2.layout.calendar_dialog_fragment, null)

            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT

            val focusable = true // lets taps outside the popup also dismiss it

            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.isOutsideTouchable = false
            popupWindow.animationStyle = R2.style.Animation

            val calendarView = popupView.findViewById<View>(R2.id.calendarView) as CalendarView
            val caloriesScoreTextView = popupView.findViewById<View>(R2.id.calTextViewData) as TextView
            val proteinsScoreTextView = popupView.findViewById<View>(R2.id.proTextViewData) as TextView
            val zsirScoreTextView = popupView.findViewById<View>(R2.id.zsirTextViewData) as TextView
            val rostScoreTextView = popupView.findViewById<View>(R2.id.rostTextViewData) as TextView
            val szenhidratScoreTextView = popupView.findViewById<View>(R2.id.szenhidratTextViewData) as TextView

            popupWindow.setOnDismissListener {
                navView.visibility = View.VISIBLE
                navView.startAnimation(slide_up)
                toggleMenu(popupView)
                hideSystemUI()
            }
            calendarView.setEvents(events)

            val min = Calendar.getInstance()
            min.set(startingYear, startingMonth, startingDay)
            min.add(Calendar.DATE, -1)

            val max = Calendar.getInstance()
            max.set(currentYear, currentMonth, currentDay)

            calendarView.setMinimumDate(min)
            calendarView.setMaximumDate(max)

            calendarView.setOnDayClickListener(object : com.applandeo.materialcalendarview.listeners.OnDayClickListener {
                override fun onDayClick(eventDay: EventDay) {
                    val clickedDayCalendarYear = eventDay.calendar.get(Calendar.YEAR)
                    val clickedDayCalendarMonth = eventDay.calendar.get(Calendar.MONTH)
                    val clickedDayCalendarDay = eventDay.calendar.get(Calendar.DATE)

                    var calString = "Nincs adat"
                    var proString = "Nincs adat"
                    var zsirString = "Nincs adat"
                    var rostString = "Nincs adat"
                    var szenhidratString = "Nincs adat"

                    for (result in dayResults){
                        if((result.year == clickedDayCalendarYear) && (result.month == clickedDayCalendarMonth) && (result.day == clickedDayCalendarDay)){
                            calString = result.scoredCalories
                            proString = result.scoredProteins
                            zsirString = result.scoredZsir
                            rostString = result.scoredRost
                            szenhidratString = result.scoredSzenhidrat
                            break
                        }
                    }
                    caloriesScoreTextView.text = calString
                    proteinsScoreTextView.text = proString
                    zsirScoreTextView.text = zsirString
                    rostScoreTextView.text = rostString
                    szenhidratScoreTextView.text = szenhidratString
                }
            })

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, -100)

            popupWindow.dimBehind()

            val clickedDayCalendarYear = calendar.get(Calendar.YEAR)
            val clickedDayCalendarMonth = calendar.get(Calendar.MONTH)
            val clickedDayCalendarDay = calendar.get(Calendar.DATE)

            var calString = "Nincs adat"
            var proString = "Nincs adat"
            var zsirString = "Nincs adat"
            var rostString = "Nincs adat"
            var szenhidratString = "Nincs adat"

            for (result in dayResults){
                if((result.year == clickedDayCalendarYear) && (result.month == clickedDayCalendarMonth) && (result.day == clickedDayCalendarDay)){
                    calString = result.scoredCalories
                    proString = result.scoredProteins
                    zsirString = result.scoredZsir
                    rostString = result.scoredRost
                    szenhidratString = result.scoredSzenhidrat
                    break
                }
            }
            caloriesScoreTextView.text = calString
            proteinsScoreTextView.text = proString
            zsirScoreTextView.text = zsirString
            rostScoreTextView.text = rostString
            szenhidratScoreTextView.text = szenhidratString
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
    fun createItemCountDialog(listToChange: MutableList<Food>, position: Int){

        fun PopupWindow.dimBehind() {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val p = container.layoutParams as WindowManager.LayoutParams
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            p.dimAmount = 0.6f
            wm.updateViewLayout(container, p)
        }

        navView.visibility = View.INVISIBLE

        val slide_down = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_down_navbar
        )

        val slide_up = AnimationUtils.loadAnimation(
            applicationContext,
            R2.anim.slide_up_navbar
        )
        navView.startAnimation(slide_down)

        // inflate the layout of the popup window
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R2.layout.itemcount_dialog_fragment, null)

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
        val itemcCountEditText = popupView.findViewById<View>(R2.id.editTextNumber1) as EditText

        itemcCountEditText.hint = "${listToChange[position].count}db"

        var itemCount: Int

        checkBtn.setOnClickListener{
            val inputText = itemcCountEditText.text.toString().trim()
            if(inputText.isNotEmpty()){
                try {
                    itemCount = inputText.toInt()
                    if(itemCount > 0) {
                        if(itemCount == 1){
                            listToChange[position].name = listToChange[position].name.substringBefore(delimiter = " (${listToChange[position].count}db)", missingDelimiterValue = listToChange[position].name)
                        }else {
                            listToChange[position].name = listToChange[position].name.substringBefore(
                                delimiter = " (${listToChange[position].count}db)",
                                missingDelimiterValue = listToChange[position].name
                            ).plus(" (${itemCount}db)")
                        }
                        listToChange[position].count = itemCount

                        var navItem1: MenuItem = navView.menu.getItem(0)
                        var navItem2: MenuItem = navView.menu.getItem(0)

                        if(navView.menu.getItem(0).isChecked) {
                            navItem1 = navView.menu.getItem(1)
                            navItem2 = navView.menu.getItem(0)
                        }
                        else if(navView.menu.getItem(1).isChecked) {
                            navItem1 = navView.menu.getItem(2)
                            navItem2 = navView.menu.getItem(1)
                        }
                        else if(navView.menu.getItem(2).isChecked) {
                            navItem1 = navView.menu.getItem(3)
                            navItem2 = navView.menu.getItem(2)
                        }
                        else if(navView.menu.getItem(3).isChecked) {
                            navItem1 = navView.menu.getItem(2)
                            navItem2 = navView.menu.getItem(3)
                        }
                        navItem1.onNavDestinationSelected(navController)
                        navItem2.onNavDestinationSelected(navController)
                    }
                } catch (e: NumberFormatException) {
                    // Handle invalid number input - do nothing, just dismiss the dialog
                }
            }

            popupWindow.dismiss()
        }

        popupWindow.setOnDismissListener {
            navView.visibility = View.VISIBLE
            navView.startAnimation(slide_up)
            this.hideSystemUI()
        }
    }



    @RequiresApi(Build.VERSION_CODES.R)
    fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun setMacros(calories: Int, proteins: Double, zsir: Double, rost: Double, szenhidrat: Double){
        if((calories >= 0) && (proteins >= 0.0)) {
            calTextView.text = "$calories"
            proTextView.text = "%.1f".format(proteins)
            // Note: New nutrient fields will be added to layout later
            // zsirTextView.text = "%.1f".format(zsir)
            // rostTextView.text = "%.1f".format(rost)
            // szenhidratTextView.text = "%.1f".format(szenhidrat)
            leftSide.visibility = View.VISIBLE
            rightSide.visibility = View.VISIBLE
        }else
        {
            calTextView.text = ""
            proTextView.text = ""
            // Note: New nutrient fields will be added to layout later
            // zsirTextView.text = ""
            // rostTextView.text = ""
            // szenhidratTextView.text = ""
            leftSide.visibility = View.INVISIBLE
            rightSide.visibility = View.INVISIBLE

        }
    }


    private fun getLists(){
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson()
        val typeOfObjectsList: Type = object : TypeToken<List<Food?>?>() {}.type

        BreakfastList = gson.fromJson(sh.getString("breakfastList", gson.toJson(BreakfastList).toString()), typeOfObjectsList)
        LunchList = gson.fromJson(sh.getString("lunchList", gson.toJson(LunchList).toString()), typeOfObjectsList)
        SnacksList = gson.fromJson(sh.getString("snackList", gson.toJson(SnacksList)), typeOfObjectsList)

        BreakfastList.sortBy { it.name }
        LunchList.sortBy { it.name }
        SnacksList.sortBy { it.name }

        calorieGoal = sh.getInt("calorieGoal", calorieGoal)
        proteinGoal = sh.getInt("proteinGoal", proteinGoal)
        zsirGoal = sh.getInt("zsirGoal", zsirGoal)
        rostGoal = sh.getInt("rostGoal", rostGoal)
        szenhidratGoal = sh.getInt("szenhidratGoal", szenhidratGoal)

        startingYear = sh.getInt("startingYear", currentYear)
        startingMonth = sh.getInt("startingMonth", currentMonth)
        startingDay = sh.getInt("startingDay", currentDay)

        isCountingCalories = sh.getBoolean("isCountingCalories", isCountingCalories)

        val typeOfResultList: Type = object : TypeToken<List<DayResult?>?>() {}.type
        dayResults = gson.fromJson(sh.getString("resultList", gson.toJson(dayResults).toString()), typeOfResultList)

        events.clear()

        for (result in dayResults){
            val calendarTemp = calendar.clone() as Calendar
            calendarTemp.set(result.year, result.month, result.day)
            if(result.isSuccess) {
                events.add(EventDay(calendarTemp, R2.drawable.ic_check_green))
            }else{
                events.add(EventDay(calendarTemp, R2.drawable.ic_cancel_red))
            }
        }

        var yearIndex = startingYear
        var monthIndex = startingMonth
        var dayIndex = startingDay
        while ((yearIndex != currentYear) || (monthIndex != currentMonth) || (dayIndex != currentDay)){
            val calendarTemp = calendar.clone() as Calendar
            calendarTemp.set(yearIndex, monthIndex, dayIndex)
            events.add(EventDay(calendarTemp, R2.drawable.ic_cancel_red))
            val calendarTempAfter = calendarTemp.clone() as Calendar
            calendarTempAfter.add(Calendar.DATE, 1)
            yearIndex = calendarTempAfter.get(Calendar.YEAR)
            monthIndex = calendarTempAfter.get(Calendar.MONTH)
            dayIndex = calendarTempAfter.get(Calendar.DATE)
        }
    }

    override fun onResume() {
        super.onResume()
        getLists()
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    fun saveData() {
        val sharedPreferences = getPreferences(Context.MODE_APPEND)
        val gson = Gson()
        val myEdit = sharedPreferences.edit()

        val TrimmedBreakfastList = BreakfastList

        for(breakfast in TrimmedBreakfastList) {
            if (breakfast.count != 1){
                breakfast.name = breakfast.name.substringBefore(delimiter = " (${breakfast.count}db)", missingDelimiterValue = breakfast.name)
                breakfast.count = 1
            }
        }

        val TrimmedLunchList = LunchList

        for(lunch in TrimmedLunchList) {
            if (lunch.count != 1){
                lunch.name = lunch.name.substringBefore(delimiter = " (${lunch.count}db)", missingDelimiterValue = lunch.name)
                lunch.count = 1
            }
        }

        val TrimmedSnacksList = SnacksList

        for(snack in TrimmedSnacksList) {
            if (snack.count != 1){
                snack.name = snack.name.substringBefore(delimiter = " (${snack.count}db)", missingDelimiterValue = snack.name)
                snack.count = 1
            }
        }

        myEdit.remove("breakfastList")
        myEdit.commit()
        myEdit.putString("breakfastList", gson.toJson(TrimmedBreakfastList))
        myEdit.commit()

        myEdit.remove("lunchList")
        myEdit.commit()
        myEdit.putString("lunchList", gson.toJson(TrimmedLunchList))
        myEdit.commit()

        myEdit.remove("snackList")
        myEdit.commit()
        myEdit.putString("snackList", gson.toJson(TrimmedSnacksList))
        myEdit.commit()

        myEdit.remove("calorieGoal")
        myEdit.commit()
        myEdit.putInt("calorieGoal", calorieGoal)
        myEdit.commit()

        myEdit.remove("proteinGoal")
        myEdit.commit()
        myEdit.putInt("proteinGoal", proteinGoal)
        myEdit.commit()

        myEdit.remove("zsirGoal")
        myEdit.commit()
        myEdit.putInt("zsirGoal", zsirGoal)
        myEdit.commit()

        myEdit.remove("rostGoal")
        myEdit.commit()
        myEdit.putInt("rostGoal", rostGoal)
        myEdit.commit()

        myEdit.remove("szenhidratGoal")
        myEdit.commit()
        myEdit.putInt("szenhidratGoal", szenhidratGoal)
        myEdit.commit()

        myEdit.remove("isCountingCalories")
        myEdit.commit()
        myEdit.putBoolean("isCountingCalories", isCountingCalories)
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

        val calendarTemp: Calendar = Calendar.getInstance()
        calendarTemp.set(newResult.year, newResult.month, newResult.day)

        for(event in events){
            if ((event.calendar.get(Calendar.YEAR) == newResult.year) &&
                (event.calendar.get(Calendar.MONTH) == newResult.month) &&
                (event.calendar.get(Calendar.DATE) == newResult.day)){
                events.remove(event)
                break
            }
        }

        // Add to lists:
        dayResults.add(newResult)
        if(newResult.isSuccess) {
            events.add(EventDay(calendarTemp, R2.drawable.ic_check_green))
        } else {
            events.add(EventDay(calendarTemp, R2.drawable.ic_cancel_red))
        }

        // Save dayResults as preferences since its footprint is smaller:
        val sh = getPreferences(Context.MODE_APPEND)
        val gson = Gson()
        val myEdit = sh.edit()

        myEdit.remove("resultList")
        myEdit.commit()
        myEdit.putString("resultList", gson.toJson(dayResults))
        myEdit.commit()

        // Save first date:
        if((sh.getInt("startingYear", currentYear) == currentYear) &&
                (sh.getInt("startingMonth", currentMonth) == currentMonth) &&
                (sh.getInt("startingDay", currentDay) == currentDay)){
            myEdit.putInt("startingYear", currentYear)
            myEdit.putInt("startingMonth", currentMonth)
            myEdit.putInt("startingDay", currentDay)
            myEdit.commit()
        }
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
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            if (e1 != null) {
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
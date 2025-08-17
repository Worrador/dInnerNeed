package whattoeat.dinner.ui.Meals

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import whattoeat.dinner.MainActivity
import whattoeat.dinner.R
import whattoeat.dinner.databinding.FragmentBreakfastBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel


class BreakfastFragment : Fragment(), View.OnTouchListener, GestureDetector.OnGestureListener {

    private var _binding: FragmentBreakfastBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false
    private var isModification: Boolean = false
    private lateinit var gestureDetector: GestureDetector
    private lateinit var listView: ListView
    /* Set objects */
    private lateinit var myActivity: MainActivity

    private lateinit var mainViewModel : MainViewModel

    private var addedCalories = 0
    private var addedProteins = 0.0

    private fun generateListView(){
        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.BreakfastList)
        context?.let {
            val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.list_text_view, listOfItem)
            listView.adapter = arrayAdapter
        }

        for (pos in mainViewModel.clickedPosListBreakfast) {
            listView.setItemChecked(pos, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myActivity = (activity as MainActivity?)!!
        mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        _binding = FragmentBreakfastBinding.inflate(inflater, container, false)
        listView = binding.breakfastListView
        val root: View = binding.root
        val nameText: TextView = binding.textView
        val caloriesText: TextView = binding.textViewCalories
        val proteinsText: TextView = binding.textViewProteins
        val zsirText: TextView = binding.textViewZsir
        val rostText: TextView = binding.textViewRost
        val szenhidratText: TextView = binding.textViewSzenhidrat
        val cancelBtn: FloatingActionButton = binding.cancelBtn
        val checkBtn: FloatingActionButton = binding.checkBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val delBtn: FloatingActionButton = binding.deleteBtn


        // Initializing the gesture detector
        gestureDetector = GestureDetector(this)

        /* Local functions */
        fun setDefaultVisibility(){
            nameText.visibility = View.INVISIBLE
            nameText.text = ""
            caloriesText.visibility = View.INVISIBLE
            caloriesText.text = ""
            proteinsText.visibility = View.INVISIBLE
            proteinsText.text = ""
            zsirText.visibility = View.INVISIBLE
            zsirText.text = ""
            rostText.visibility = View.INVISIBLE
            rostText.text = ""
            szenhidratText.visibility = View.INVISIBLE
            szenhidratText.text = ""
            checkBtn.visibility = View.INVISIBLE
            cancelBtn.visibility = View.INVISIBLE
            addBtn.visibility = View.VISIBLE
            delBtn.visibility = View.VISIBLE
            root.hideKeyboard()
            myActivity.hideSystemUI()
            calculateAddedMacros()
            isModification= false
        }

        fun setModifyingVisibility(){
            if(isDataAddition){
                nameText.visibility = View.VISIBLE
                caloriesText.visibility = View.VISIBLE
                proteinsText.visibility = View.VISIBLE
                zsirText.visibility = View.VISIBLE
                rostText.visibility = View.VISIBLE
                szenhidratText.visibility = View.VISIBLE
            }
            calculateAddedMacros()
            addBtn.visibility = View.INVISIBLE
            delBtn.visibility = View.INVISIBLE
            checkBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
        }

        /* Set object callbacks */
        addBtn.setOnClickListener {
            isDataAddition = true
            isModification = true
            setModifyingVisibility()
        }

        delBtn.setOnClickListener {
            isDataAddition = false
            isModification = true
            for (pos in mainViewModel.clickedPosListBreakfast)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListBreakfast.clear()
            setModifyingVisibility()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = !TextUtils.isEmpty(nameText.text) && !TextUtils.isEmpty(caloriesText.text) && !TextUtils.isEmpty(proteinsText.text) && !TextUtils.isEmpty(zsirText.text) && !TextUtils.isEmpty(rostText.text) && !TextUtils.isEmpty(szenhidratText.text)
                if(isGoodInputs){
                    Toast.makeText(
                        context,
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.BreakfastList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toDouble(), zsirText.text.toString().toDouble(), rostText.text.toString().toDouble(), szenhidratText.text.toString().toDouble()))
                    setDefaultVisibility()
                    this.generateListView()
                }else{
                    Toast.makeText(
                        context,
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                var flag = false
                for (pos in mainViewModel.clickedPosListBreakfast){
                    val foodToDeleteName = listView.getItemAtPosition(pos)
                    for(food in myActivity.BreakfastList){
                        if(food.name == foodToDeleteName) {
                            flag = flag or myActivity.BreakfastList.remove(food)
                            break
                        }
                    }
                }

                if(flag){
                    Toast.makeText(
                        context,
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListBreakfast.clear()
                    this.generateListView()
                }
                setDefaultVisibility()
            }
        }

        cancelBtn.setOnClickListener {
            if(isDataAddition.not()) {
                for (pos in mainViewModel.clickedPosListBreakfast)
                    listView.setItemChecked(pos, false)
                mainViewModel.clickedPosListBreakfast.clear()
            }
            setDefaultVisibility()
        }

        /* Set listView */
        this.generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.setOnTouchListener(this)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (mainViewModel.clickedPosListBreakfast.contains(position)) {
                    mainViewModel.clickedPosListBreakfast.remove(position)
                }
                else {
                    mainViewModel.clickedPosListBreakfast.add(position)
                }

                if(!isModification) {
                    calculateAddedMacros()
                }
                true
            }
        listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener  { _, _, position, _ ->
                if(ViewConfiguration.getPressedStateDuration() >= 2)
                    myActivity.createItemCountDialog(myActivity.BreakfastList, position)
                true
            }
        for (pos in mainViewModel.clickedPosListBreakfast) {
            listView.setItemChecked(pos, true)
        }
        calculateAddedMacros()

        return root
    }

    private fun calculateAddedMacros(){
        /* Set objects */
        val myActivity = (activity as MainActivity?)!!

        val mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        addedCalories = 0
        addedProteins = 0.0
        var addedZsir = 0.0
        var addedRost = 0.0
        var addedSzenhidrat = 0.0

        for (pos in mainViewModel.clickedPosListBreakfast){
            addedCalories += myActivity.BreakfastList[pos].calories * myActivity.BreakfastList[pos].count
            addedProteins += myActivity.BreakfastList[pos].proteins * myActivity.BreakfastList[pos].count
            addedZsir += myActivity.BreakfastList[pos].zsir * myActivity.BreakfastList[pos].count
            addedRost += myActivity.BreakfastList[pos].rost * myActivity.BreakfastList[pos].count
            addedSzenhidrat += myActivity.BreakfastList[pos].szenhidrat * myActivity.BreakfastList[pos].count
        }
        myActivity.setMacros(addedCalories, addedProteins, addedZsir, addedRost, addedSzenhidrat)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTouch(v: View?, e: MotionEvent?): Boolean {
        return e?.let { gestureDetector.onTouchEvent(it) } ?: false
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
        val myActivity = (activity as MainActivity?)!!
        return myActivity.onFling(e1, e2, velocityX, velocityY)
    }
}

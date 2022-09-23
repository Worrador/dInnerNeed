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
import whattoeat.dinner.databinding.FragmentLunchBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel


class LunchFragment : Fragment(), View.OnTouchListener, GestureDetector.OnGestureListener {

    private var _binding: FragmentLunchBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false
    private var isModification: Boolean = false
    private lateinit var gestureDetector: GestureDetector

    private var addedCalories = 0
    private var addedProteins = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /* Set objects */
        val myActivity = (activity as MainActivity?)!!

        val mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        _binding = FragmentLunchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val nameText: TextView = binding.textView
        val caloriesText: TextView = binding.textViewCalories
        val proteinsText: TextView = binding.textViewProteins
        val cancelBtn: FloatingActionButton = binding.cancelBtn
        val checkBtn: FloatingActionButton = binding.checkBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val delBtn: FloatingActionButton = binding.deleteBtn
        val listView: ListView = binding.lunchListView

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
            }
            calculateAddedMacros()
            addBtn.visibility = View.INVISIBLE
            delBtn.visibility = View.INVISIBLE
            checkBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
        }

        fun generateListView(){
            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.LunchList)
            context?.let {
                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.list_text_view, listOfItem)
                listView.adapter = arrayAdapter
            }

            for (pos in mainViewModel.clickedPosListLunch) {
                listView.setItemChecked(pos, true)
            }
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
            for (pos in mainViewModel.clickedPosListLunch)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListLunch.clear()
            setModifyingVisibility()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = TextUtils.isDigitsOnly(caloriesText.text) && TextUtils.isDigitsOnly(proteinsText.text) &&
                        !TextUtils.isEmpty(nameText.text) && !TextUtils.isEmpty(caloriesText.text) && !TextUtils.isEmpty(proteinsText.text)
                if(isGoodInputs){
                    Toast.makeText(
                        context,
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.LunchList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toDouble()))
                    myActivity.LunchList.sortBy{it.name}
                    setDefaultVisibility()
                    generateListView()
                }else{
                    Toast.makeText(
                        context,
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                var flag = false
                for (pos in mainViewModel.clickedPosListLunch){
                    val foodToDeleteName = listView.getItemAtPosition(pos)
                    for(food in myActivity.LunchList){
                        if(food.name == foodToDeleteName) {
                            flag = flag or myActivity.LunchList.remove(food)
                            break
                        }
                    }
                }

                if(flag){
                    Toast.makeText(
                        context,
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListLunch.clear()
                    generateListView()
                }
                setDefaultVisibility()
            }
        }

        cancelBtn.setOnClickListener {
            if(isDataAddition.not()) {
                for (pos in mainViewModel.clickedPosListLunch)
                    listView.setItemChecked(pos, false)
                mainViewModel.clickedPosListLunch.clear()
            }
            setDefaultVisibility()
        }

        /* Set listView */
        generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.setOnTouchListener(this)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (mainViewModel.clickedPosListLunch.contains(position)) {
                    mainViewModel.clickedPosListLunch.remove(position)
                }
                else {
                    mainViewModel.clickedPosListLunch.add(position)
                }

                if(!isModification) {
                    calculateAddedMacros()
                }
            }
        listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener  { _, _, position, _ ->
                if(android.view.ViewConfiguration.getPressedStateDuration() >= 2)
                    myActivity.createItemCountDialog(myActivity.LunchList, position)
                true
            }
        for (pos in mainViewModel.clickedPosListLunch) {
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

        for (pos in mainViewModel.clickedPosListLunch){
            addedCalories += myActivity.LunchList[pos].calories * myActivity.LunchList[pos].count
            addedProteins += myActivity.LunchList[pos].proteins * myActivity.LunchList[pos].count
        }
        myActivity.setMacros(addedCalories, addedProteins)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onTouch(v: View?, e: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(e)
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
        val myActivity = (activity as MainActivity?)!!
        return myActivity.onFling(e1, e2, velocityX, velocityY)
    }
}

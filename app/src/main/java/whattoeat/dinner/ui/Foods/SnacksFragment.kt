package whattoeat.dinner.ui.Foods
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import whattoeat.dinner.Food
import whattoeat.dinner.MainActivity
import whattoeat.dinner.R
import whattoeat.dinner.databinding.FragmentSnacksBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel


class SnacksFragment : Fragment() {

    private var _binding: FragmentSnacksBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false
    private var isModification: Boolean = false

    private var addedCalories = 0
    private var addedProteins = 0

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

        _binding = FragmentSnacksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val nameText: TextView = binding.textView
        val caloriesText: TextView = binding.textViewCalories
        val proteinsText: TextView = binding.textViewProteins
        val cancelBtn: FloatingActionButton = binding.cancelBtn
        val checkBtn: FloatingActionButton = binding.checkBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val delBtn: FloatingActionButton = binding.deleteBtn
        val listView: ListView = binding.snacksListView

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
            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnacksList)
            context?.let {
                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.list_text_view, listOfItem)
                listView.adapter = arrayAdapter
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
            for (pos in mainViewModel.clickedPosListSnacks)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListSnacks.clear()
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
                    myActivity.SnacksList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toInt()))
                    setDefaultVisibility()
                    generateListView()
                }else{
                    Toast.makeText(
                        context,
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                var flag = false
                for (pos in mainViewModel.clickedPosListSnacks){
                    val foodToDeleteName = listView.getItemAtPosition(pos)
                    for(food in myActivity.SnacksList){
                        if(food.name == foodToDeleteName) {
                            flag = flag or myActivity.SnacksList.remove(food)
                            break
                        }
                    }
                }

                if(flag){
                    Toast.makeText(
                        context,
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListSnacks.clear()
                    generateListView()
                }
                setDefaultVisibility()
            }
        }

        cancelBtn.setOnClickListener {
            if(isDataAddition.not()) {
                for (pos in mainViewModel.clickedPosListSnacks)
                    listView.setItemChecked(pos, false)
                mainViewModel.clickedPosListSnacks.clear()
            }
            setDefaultVisibility()
        }

        /* Set listView */
        generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (mainViewModel.clickedPosListSnacks.contains(position)) {
                    mainViewModel.clickedPosListSnacks.remove(position)
                }
                else {
                    mainViewModel.clickedPosListSnacks.add(position)
                }

                if(!isModification) {
                    calculateAddedMacros()
                }
            }
        for (pos in mainViewModel.clickedPosListSnacks) {
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
        addedProteins = 0

        for (pos in mainViewModel.clickedPosListSnacks){
            addedCalories += myActivity.SnacksList[pos].calories
            addedProteins += myActivity.SnacksList[pos].proteins
        }
        myActivity.setMacros(addedCalories, addedProteins)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

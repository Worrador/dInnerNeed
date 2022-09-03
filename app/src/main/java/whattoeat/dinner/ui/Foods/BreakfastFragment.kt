package whattoeat.dinner.ui.Foods
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
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
import whattoeat.dinner.databinding.FragmentBreakfastBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel


class BreakfastFragment : Fragment() {

    private var _binding: FragmentBreakfastBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false
    private var isModification: Boolean = false

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

        _binding = FragmentBreakfastBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val nameText: TextView = binding.textView
        val caloriesText: TextView = binding.textViewCalories
        val proteinsText: TextView = binding.textViewProteins
        val cancelBtn: FloatingActionButton = binding.cancelBtn
        val checkBtn: FloatingActionButton = binding.checkBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val delBtn: FloatingActionButton = binding.deleteBtn
        val listView: ListView = binding.breakfastListView

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
            isModification= false
        }

        fun setModifyingVisibility(){
            if(isDataAddition){
                nameText.visibility = View.VISIBLE
                caloriesText.visibility = View.VISIBLE
                proteinsText.visibility = View.VISIBLE
            }

            addBtn.visibility = View.INVISIBLE
            delBtn.visibility = View.INVISIBLE
            checkBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
        }

        fun generateListView(){
            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.BreakfastList)
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
            setModifyingVisibility()
            for (pos in mainViewModel.clickedPosListBreakfast)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListBreakfast.clear()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = TextUtils.isDigitsOnly(caloriesText.text) && TextUtils.isDigitsOnly(proteinsText.text) &&
                        !TextUtils.isEmpty(nameText.text) && !TextUtils.isEmpty(caloriesText.text) && !TextUtils.isEmpty(proteinsText.text)
                if(isGoodInputs){
                    Toast.makeText(
                        context,
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.BreakfastList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toInt()))
                    setDefaultVisibility()
                    generateListView()
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
                    generateListView()
                }
                setDefaultVisibility()
            }
        }

        cancelBtn.setOnClickListener {
            setDefaultVisibility()
        }

        /* Set listView */
        generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (mainViewModel.clickedPosListBreakfast.contains(position)) {
                    mainViewModel.clickedPosListBreakfast.remove(position)
                }
                else {
                    if(!isModification) {
                        val clickedCalories = myActivity.BreakfastList[position].calories
                        val clickedProteins = myActivity.BreakfastList[position].proteins
                        Toast.makeText(
                            context,
                            "Kalória: +$clickedCalories\nFehérje: +$clickedProteins\n",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    mainViewModel.clickedPosListBreakfast.add(position)
                }
            }
        for (pos in mainViewModel.clickedPosListBreakfast) {
            listView.setItemChecked(pos, true)
        }

        return root 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

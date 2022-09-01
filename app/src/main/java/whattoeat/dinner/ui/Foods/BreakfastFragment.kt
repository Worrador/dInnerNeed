package whattoeat.dinner.ui.Foods
import android.graphics.Color
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
import whattoeat.dinner.databinding.FragmentBreakfastBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.R as R2


class BreakfastFragment : Fragment() {

    private var _binding: FragmentBreakfastBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false

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
            nameText.setVisibility(View.INVISIBLE)
            nameText.setText("")
            caloriesText.setVisibility(View.INVISIBLE)
            caloriesText.setText("")
            proteinsText.setVisibility(View.INVISIBLE)
            proteinsText.setText("")
            checkBtn.visibility = View.INVISIBLE
            cancelBtn.visibility = View.INVISIBLE
            addBtn.visibility = View.VISIBLE
            delBtn.visibility = View.VISIBLE
            root.hideKeyboard()
        }

        fun setModifyingVisibility(){
            if(isDataAddition){
                nameText.setVisibility(View.VISIBLE)
                caloriesText.setVisibility(View.VISIBLE)
                proteinsText.setVisibility(View.VISIBLE)
            }

            addBtn.visibility = View.INVISIBLE
            delBtn.visibility = View.INVISIBLE
            checkBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
        }

        fun generateListView(){
            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.BreakfastList)
            getContext()?.let {
                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R2.layout.list_text_view, listOfItem)
                listView.adapter = arrayAdapter
            }
        }

        /* Set object callbacks */
        addBtn.setOnClickListener {
            isDataAddition = true
            setModifyingVisibility()
        }

        delBtn.setOnClickListener {
            isDataAddition = false
            setModifyingVisibility()
            for (pos in mainViewModel.clickedPosListBreakfast)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListBreakfast.clear()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = TextUtils.isDigitsOnly(caloriesText.getText()) && TextUtils.isDigitsOnly(proteinsText.getText()) &&
                        !TextUtils.isEmpty(nameText.getText()) && !TextUtils.isEmpty(caloriesText.getText()) && !TextUtils.isEmpty(proteinsText.getText())
                if(isGoodInputs){
                    Toast.makeText(getContext(),
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.BreakfastList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toInt()))
                    setDefaultVisibility()
                    generateListView()
                }else{
                    Toast.makeText(getContext(),
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                var flag: Boolean = false
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
                    Toast.makeText(getContext(),
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
                    view.setBackgroundColor(Color.parseColor("#d1e659"))
                    val clickedCalories = myActivity.BreakfastList[position].calories
                    val clickedProteins = myActivity.BreakfastList[position].proteins
                    Toast.makeText(
                        getContext(),
                        "Kalória: +$clickedCalories\nFehérje: +$clickedProteins\n",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainViewModel.clickedPosListBreakfast.add(position)
                }
            }
        for (pos in mainViewModel.clickedPosListBreakfast)
            listView.setItemChecked(pos, true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
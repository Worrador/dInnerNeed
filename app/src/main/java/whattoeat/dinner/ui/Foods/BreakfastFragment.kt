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
        myActivity.getLists()

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
        fun setDefaultVisivility(){
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

        fun setModifyingVisivility(){
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
            setModifyingVisivility()
        }

        delBtn.setOnClickListener {
            isDataAddition = false
            setModifyingVisivility()
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
                    myActivity.BreakfastList.add(nameText.text.toString())
                    setDefaultVisivility()
                    generateListView()
                }else{
                    Toast.makeText(getContext(),
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                for (pos in mainViewModel.clickedPosListBreakfast)
                    myActivity.BreakfastList.remove(listView.getItemAtPosition(pos))

                if(!mainViewModel.clickedPosListBreakfast.isEmpty()){
                    Toast.makeText(getContext(),
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListBreakfast.clear()
                    generateListView()
                }
                setDefaultVisivility()
            }
        }

        cancelBtn.setOnClickListener {
            setDefaultVisivility()
        }

        /* Set listView */
        generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (mainViewModel.clickedPosListBreakfast.contains(position))
                    mainViewModel.clickedPosListBreakfast.remove(position)
                else
                    mainViewModel.clickedPosListBreakfast.add(position)
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
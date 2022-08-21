package whattoeat.dinner.ui.Foods

import android.R
import android.content.Context
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
import whattoeat.dinner.databinding.FragmentSnacksBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.R as R2


class SnacksFragment : Fragment() {

    private var _binding: FragmentSnacksBinding? = null
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
            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnackList)
            getContext()?.let {
                val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R2.layout.list_text_view, listOfItem)
                listView.adapter = arrayAdapter
            }
            for (pos in mainViewModel.clickedPosListSnack)
                listView.setItemChecked(pos, true)
        }

        /* Set object callbacks */
        addBtn.setOnClickListener {
            isDataAddition = true
            setModifyingVisivility()
        }

        delBtn.setOnClickListener {
            isDataAddition = false
            setModifyingVisivility()
            for (pos in mainViewModel.clickedPosListSnack)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListSnack.clear()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = TextUtils.isDigitsOnly(caloriesText.getText()) && TextUtils.isDigitsOnly(proteinsText.getText()) &&
                        !TextUtils.isEmpty(nameText.getText()) && !TextUtils.isEmpty(caloriesText.getText()) && !TextUtils.isEmpty(proteinsText.getText())
                if(isGoodInputs){
                    Toast.makeText(getContext(),
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.SnackList.add(nameText.text.toString())
                    setDefaultVisivility()
                    generateListView()
                }else{
                    Toast.makeText(getContext(),
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                for (pos in mainViewModel.clickedPosListSnack)
                    myActivity.SnackList.remove(listView.getItemAtPosition(pos))

                if(!mainViewModel.clickedPosListSnack.isEmpty()){
                    Toast.makeText(getContext(),
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListSnack.clear()
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
                if (mainViewModel.clickedPosListSnack.contains(position))
                    mainViewModel.clickedPosListSnack.remove(position)
                else
                    mainViewModel.clickedPosListSnack.add(position)
            }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
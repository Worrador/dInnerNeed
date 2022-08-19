package whattoeat.dinner.ui.Foods

import android.R
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


class SnacksFragment : Fragment() {

    private var _binding: FragmentSnacksBinding? = null
    private val binding get() = _binding!!

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
        val saveBtn: Button = binding.saveBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val listView: ListView = binding.breakfastListView
        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnackList)

        /* Set object properties */
        addBtn.setOnClickListener {
            nameText.setVisibility(View.VISIBLE)
            caloriesText.setVisibility(View.VISIBLE)
            proteinsText.setVisibility(View.VISIBLE)
            addBtn.visibility = View.INVISIBLE
            saveBtn.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            val isGoodInputs = TextUtils.isDigitsOnly(caloriesText.getText()) && TextUtils.isDigitsOnly(proteinsText.getText()) &&
                !TextUtils.isEmpty(nameText.getText()) && !TextUtils.isEmpty(caloriesText.getText()) && !TextUtils.isEmpty(proteinsText.getText())
            if(isGoodInputs){
                Toast.makeText(getContext(),
                    "Hozzáadva!", Toast.LENGTH_SHORT).show()
                myActivity.SnackList.add(nameText.text.toString())
                nameText.setVisibility(View.INVISIBLE)
                nameText.setText("")
                caloriesText.setVisibility(View.INVISIBLE)
                caloriesText.setText("")
                proteinsText.setVisibility(View.INVISIBLE)
                proteinsText.setText("")
                saveBtn.visibility = View.INVISIBLE
                addBtn.visibility = View.VISIBLE
                val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnackList)
                getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.simple_list_item_multiple_choice, listOfItem)
                    listView.adapter = arrayAdapter
                }
                root.hideKeyboard()
            }else{
                Toast.makeText(getContext(),
                    "Helytelen értékek!", Toast.LENGTH_SHORT).show()
            }
        }

        getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.simple_list_item_multiple_choice, listOfItem)
            listView.adapter = arrayAdapter
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (mainViewModel.clickedPosListSnack.contains(position))
                    mainViewModel.clickedPosListSnack.remove(position)
                else
                    mainViewModel.clickedPosListSnack.add(position)
            }

        for (pos in mainViewModel.clickedPosListSnack)
            listView.setItemChecked(pos, true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
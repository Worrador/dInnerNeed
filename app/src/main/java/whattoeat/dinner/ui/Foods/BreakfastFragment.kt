package whattoeat.dinner.ui.Foods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.MainActivity
import whattoeat.dinner.R
import whattoeat.dinner.databinding.FragmentBreakfastBinding
import whattoeat.dinner.ui.MainViewModel


class BreakfastFragment : Fragment(R.layout.fragment_breakfast) {

    private var _binding: FragmentBreakfastBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val myActivity = (activity as MainActivity?)!!
        myActivity.getLists()


        val mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        _binding = FragmentBreakfastBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textView
        val saveBtn: Button = binding.saveBtn

        val listView: ListView = binding.breakfastListView

        saveBtn.setOnClickListener {
            myActivity.BreakfastList.add(textView.text.toString())

            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.BreakfastList)

            getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, android.R.layout.simple_list_item_multiple_choice, listOfItem)
                listView.adapter = arrayAdapter
            }
        }


        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.BreakfastList)

        getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, android.R.layout.simple_list_item_multiple_choice, listOfItem)
            listView.adapter = arrayAdapter
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                if(mainViewModel.clickedPosListBreakfast.contains(position))
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
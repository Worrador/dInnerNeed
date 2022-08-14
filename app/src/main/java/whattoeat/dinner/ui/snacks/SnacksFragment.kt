package whattoeat.dinner.ui.snacks

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.MainActivity
import whattoeat.dinner.databinding.FragmentDashboardBinding
import whattoeat.dinner.ui.MainViewModel

class SnacksFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

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

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textView
        val saveBtn: Button = binding.saveBtn

        val listView: ListView = binding.breakfastListView

        saveBtn.setOnClickListener {
            myActivity.SnackList.add(textView.text.toString())

            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnackList)

            getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.simple_list_item_multiple_choice, listOfItem)
                listView.adapter = arrayAdapter
            }
        }


        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnackList)

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
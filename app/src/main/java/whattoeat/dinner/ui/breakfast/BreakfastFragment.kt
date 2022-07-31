package whattoeat.dinner.ui.breakfast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.databinding.FragmentHomeBinding
import whattoeat.dinner.ui.BreakfastViewModel


class BreakfastFragment : Fragment(whattoeat.dinner.R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val breakfastViewModel = activity?.run {
            ViewModelProvider(this)[BreakfastViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: ListView = binding.breakfastListView
        val listOfItem: ArrayList<String> = breakfastViewModel.setMultipleListView()

        getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, android.R.layout.simple_list_item_multiple_choice, listOfItem)
            listView.adapter = arrayAdapter
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                if(breakfastViewModel.positionClickedArrayList.contains(position))
                    breakfastViewModel.positionClickedArrayList.remove(position)
                else
                    breakfastViewModel.positionClickedArrayList.add(position)
            }

        for (pos in breakfastViewModel.positionClickedArrayList)
            listView.setItemChecked(pos, true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
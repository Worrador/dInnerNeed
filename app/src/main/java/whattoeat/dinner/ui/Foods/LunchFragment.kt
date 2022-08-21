package whattoeat.dinner.ui.Foods

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.MainActivity
import whattoeat.dinner.databinding.FragmentLunchBinding
import whattoeat.dinner.ui.MainViewModel
import whattoeat.dinner.R as R2

class LunchFragment : Fragment() {

    private var _binding: FragmentLunchBinding? = null

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

        _binding = FragmentLunchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textView
        val saveBtn: Button = binding.saveBtn

        val listView: ListView = binding.breakfastListView

        saveBtn.setOnClickListener {
            myActivity.LunchList.add(textView.text.toString())

            val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.LunchList)

            getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R2.layout.list_text_view, listOfItem)
                listView.adapter = arrayAdapter
            }
        }


        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.LunchList)

        getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R2.layout.list_text_view, listOfItem)
            listView.adapter = arrayAdapter
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (mainViewModel.clickedPosListLunch.contains(position))
                    mainViewModel.clickedPosListLunch.remove(position)
                else
                    mainViewModel.clickedPosListLunch.add(position)
            }

        for (pos in mainViewModel.clickedPosListLunch)
            listView.setItemChecked(pos, true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
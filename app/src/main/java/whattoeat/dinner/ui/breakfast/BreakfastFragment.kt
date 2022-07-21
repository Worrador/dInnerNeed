package whattoeat.dinner.ui.breakfast

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import whattoeat.dinner.databinding.FragmentHomeBinding


class BreakfastFragment : Fragment(), AdapterView.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun setMultipleListView(): ArrayList<String>{
        val arrayList: ArrayList<String> = ArrayList()

        for(i in 1..10){
            arrayList.add("App list  :D $i")
        }
        return arrayList
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val breakfastViewModel =
            ViewModelProvider(this).get(BreakfastViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: ListView = binding.breakfastListView
        val listOfItem: ArrayList<String> = setMultipleListView()
        getContext()?.let { val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, android.R.layout.simple_list_item_multiple_choice, listOfItem)
            listView.adapter = arrayAdapter
        }

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.onItemClickListener = this

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val options: String = parent?.getItemAtPosition(position) as String
        if (view != null) {
            Toast.makeText(view.getContext(), "Clicked By: $options", Toast.LENGTH_LONG).show()
        }
    }
}
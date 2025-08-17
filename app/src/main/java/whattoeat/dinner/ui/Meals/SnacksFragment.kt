package whattoeat.dinner.ui.Meals
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import whattoeat.dinner.MainActivity
import whattoeat.dinner.R
import whattoeat.dinner.databinding.FragmentSnacksBinding
import whattoeat.dinner.hideKeyboard
import whattoeat.dinner.ui.MainViewModel
import java.io.BufferedReader
import java.io.InputStreamReader


class SnacksFragment : Fragment(), View.OnTouchListener, GestureDetector.OnGestureListener {

    private var _binding: FragmentSnacksBinding? = null
    private val binding get() = _binding!!
    private var isDataAddition: Boolean = false
    private var isModification: Boolean = false
    private lateinit var gestureDetector: GestureDetector
    private lateinit var listView: ListView
    /* Set objects */
    private lateinit var myActivity: MainActivity

    private lateinit var mainViewModel : MainViewModel

    private var addedCalories = 0
    private var addedProteins = 0.0

    // CSV Import functionality
    private val csvFilePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importCsvFromUri(it) }
    }

    private fun importCsvFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var lineNumber = 0
            var importedCount = 0

            reader.use { bufferedReader ->
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    lineNumber++
                    if (lineNumber == 1) continue // Skip header row

                    line?.let { csvLine ->
                        val food = parseCsvLine(csvLine)
                        if (food != null) {
                            myActivity.SnacksList.add(food)
                            importedCount++
                        }
                    }
                }
            }

            if (importedCount > 0) {
                Toast.makeText(context, getString(R.string.csv_import_success) + " ($importedCount étel)", Toast.LENGTH_LONG).show()
                generateListView()
                updateImportButtonVisibility()

                // Force save the data immediately after import
                myActivity.saveData()
            } else {
                Toast.makeText(context, getString(R.string.csv_format_error), Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.csv_import_error) + ": ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun parseCsvLine(csvLine: String): Food? {
        return try {
            // Clean the line - remove trailing spaces and check if empty
            val cleanLine = csvLine.trim()
            if (cleanLine.isEmpty()) {
                println("Skipping empty line")
                return null
            }

            // Expected CSV format: name,calories,proteins,zsir,rost,szenhidrat
            val columns = cleanLine.split(",").map { it.trim() }

            // Debug logging
            println("Parsing CSV line: '$cleanLine'")
            println("Columns found: ${columns.size}")
            columns.forEachIndexed { index, col -> println("Column $index: '$col'") }

            if (columns.size >= 6) {
                val name = columns[0]
                val calories = columns[1].toInt()
                val proteins = columns[2].toDouble()
                val zsir = columns[3].toDouble()
                val rost = columns[4].toDouble()
                val szenhidrat = columns[5].toDouble()

                println("Successfully parsed: $name, $calories, $proteins, $zsir, $rost, $szenhidrat")
                Food(name, calories, proteins, zsir, rost, szenhidrat)
            } else {
                println("Error: Expected 6 columns, found ${columns.size}")
                null
            }
        } catch (e: Exception) {
            println("Error parsing CSV line '$csvLine': ${e.message}")
            null
        }
    }

    private fun updateImportButtonVisibility() {
        val importButton = binding.importCsvBtn
        if (myActivity.SnacksList.isEmpty()) {
            importButton.visibility = View.VISIBLE
        } else {
            importButton.visibility = View.GONE
        }
    }

    private fun generateListView(){
        val listOfItem: ArrayList<String> = mainViewModel.setMultipleListView(myActivity.SnacksList)
        context?.let {
            val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.list_text_view, listOfItem)
            listView.adapter = arrayAdapter
        }

        for (pos in mainViewModel.clickedPosListSnacks) {
            listView.setItemChecked(pos, true)
        }

        // Update import button visibility
        updateImportButtonVisibility()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /* Set objects */
        myActivity = (activity as MainActivity?)!!

        mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        _binding = FragmentSnacksBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val nameText: TextView = binding.textView
        val caloriesText: TextView = binding.textViewCalories
        val proteinsText: TextView = binding.textViewProteins
        val zsirText: TextView = binding.textViewZsir
        val rostText: TextView = binding.textViewRost
        val szenhidratText: TextView = binding.textViewSzenhidrat
        val cancelBtn: FloatingActionButton = binding.cancelBtn
        val checkBtn: FloatingActionButton = binding.checkBtn
        val addBtn: FloatingActionButton = binding.addBtn
        val delBtn: FloatingActionButton = binding.deleteBtn
        listView = binding.snacksListView
        val importCsvBtn: MaterialButton = binding.importCsvBtn

        // Initializing the gesture detector
        gestureDetector = GestureDetector(this)

        // Setup import CSV button
        importCsvBtn.setOnClickListener {
            csvFilePicker.launch("text/csv")
        }

        /* Local functions */
        fun setDefaultVisibility(){
            nameText.visibility = View.INVISIBLE
            nameText.text = ""
            caloriesText.visibility = View.INVISIBLE
            caloriesText.text = ""
            proteinsText.visibility = View.INVISIBLE
            proteinsText.text = ""
            zsirText.visibility = View.INVISIBLE
            zsirText.text = ""
            rostText.visibility = View.INVISIBLE
            rostText.text = ""
            szenhidratText.visibility = View.INVISIBLE
            szenhidratText.text = ""
            checkBtn.visibility = View.INVISIBLE
            cancelBtn.visibility = View.INVISIBLE
            addBtn.visibility = View.VISIBLE
            delBtn.visibility = View.VISIBLE
            root.hideKeyboard()
            myActivity.hideSystemUI()
            calculateAddedMacros()
            isModification= false
        }

        fun setModifyingVisibility(){
            if(isDataAddition){
                nameText.visibility = View.VISIBLE
                caloriesText.visibility = View.VISIBLE
                proteinsText.visibility = View.VISIBLE
                zsirText.visibility = View.VISIBLE
                rostText.visibility = View.VISIBLE
                szenhidratText.visibility = View.VISIBLE
            }
            calculateAddedMacros()
            addBtn.visibility = View.INVISIBLE
            delBtn.visibility = View.INVISIBLE
            checkBtn.visibility = View.VISIBLE
            cancelBtn.visibility = View.VISIBLE
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
            for (pos in mainViewModel.clickedPosListSnacks)
                listView.setItemChecked(pos, false)
            mainViewModel.clickedPosListSnacks.clear()
            setModifyingVisibility()
        }

        checkBtn.setOnClickListener {
            if(isDataAddition){
                val isGoodInputs = !TextUtils.isEmpty(nameText.text) && !TextUtils.isEmpty(caloriesText.text) && !TextUtils.isEmpty(proteinsText.text) && !TextUtils.isEmpty(zsirText.text) && !TextUtils.isEmpty(rostText.text) && !TextUtils.isEmpty(szenhidratText.text)
                if(isGoodInputs){
                    Toast.makeText(
                        context,
                        "Hozzáadva!", Toast.LENGTH_SHORT).show()
                    myActivity.SnacksList.add(Food(nameText.text.toString(), caloriesText.text.toString().toInt(), proteinsText.text.toString().toDouble(), zsirText.text.toString().toDouble(), rostText.text.toString().toDouble(), szenhidratText.text.toString().toDouble()))
                    setDefaultVisibility()
                    generateListView()
                    updateImportButtonVisibility()
                }else{
                    Toast.makeText(
                        context,
                        "Helytelen értékek!", Toast.LENGTH_SHORT).show()
                }
            }else{
                var flag = false
                for (pos in mainViewModel.clickedPosListSnacks){
                    val foodToDeleteName = listView.getItemAtPosition(pos)
                    for(food in myActivity.SnacksList){
                        if(food.name == foodToDeleteName) {
                            flag = flag or myActivity.SnacksList.remove(food)
                            break
                        }
                    }
                }

                if(flag){
                    Toast.makeText(
                        context,
                        "Törölve!", Toast.LENGTH_SHORT).show()
                    mainViewModel.clickedPosListSnacks.clear()
                    generateListView()
                    updateImportButtonVisibility()
                }
                setDefaultVisibility()
            }
        }

        cancelBtn.setOnClickListener {
            if(isDataAddition.not()) {
                for (pos in mainViewModel.clickedPosListSnacks)
                    listView.setItemChecked(pos, false)
                mainViewModel.clickedPosListSnacks.clear()
            }
            setDefaultVisibility()
        }

        /* Set listView */
        generateListView()
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.setOnTouchListener(this)
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (mainViewModel.clickedPosListSnacks.contains(position)) {
                    mainViewModel.clickedPosListSnacks.remove(position)
                }
                else {
                    mainViewModel.clickedPosListSnacks.add(position)
                }

                if(!isModification) {
                    calculateAddedMacros()
                }
            }
        listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener  { _, _, position, _ ->
                if(ViewConfiguration.getPressedStateDuration() >= 2)
                    myActivity.createItemCountDialog(myActivity.SnacksList, position)
                true
            }
        for (pos in mainViewModel.clickedPosListSnacks) {
            listView.setItemChecked(pos, true)
        }
        calculateAddedMacros()

        // Initial import button visibility check
        updateImportButtonVisibility()

        return root
    }

    private fun calculateAddedMacros(){
        /* Set objects */
        val myActivity = (activity as MainActivity?)!!

        val mainViewModel = activity?.run {
            ViewModelProvider(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        addedCalories = 0
        addedProteins = 0.0
        var addedZsir = 0.0
        var addedRost = 0.0
        var addedSzenhidrat = 0.0

        for (pos in mainViewModel.clickedPosListSnacks){
            addedCalories += myActivity.SnacksList[pos].calories * myActivity.SnacksList[pos].count
            addedProteins += myActivity.SnacksList[pos].proteins * myActivity.SnacksList[pos].count
            addedZsir += myActivity.SnacksList[pos].zsir * myActivity.SnacksList[pos].count
            addedRost += myActivity.SnacksList[pos].rost * myActivity.SnacksList[pos].count
            addedSzenhidrat += myActivity.SnacksList[pos].szenhidrat * myActivity.SnacksList[pos].count
        }
        myActivity.setMacros(addedCalories, addedProteins, addedZsir, addedRost, addedSzenhidrat)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onTouch(v: View?, e: MotionEvent?): Boolean {
        return e?.let { gestureDetector.onTouchEvent(it) } ?: false
    }

    // All the below methods are GestureDetector.OnGestureListener members
    // Except onFling, all must "return false" if Boolean return type
    // and "return" if no return type
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val myActivity = (activity as MainActivity?)!!
        return myActivity.onFling(e1, e2, velocityX, velocityY)
    }
}

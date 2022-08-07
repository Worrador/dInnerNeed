package whattoeat.dinner

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import whattoeat.dinner.databinding.ActivityMainBinding
import whattoeat.dinner.ui.MainViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var mainViewModel : MainViewModel
    var BreakfastList: MutableSet<String> = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLists()

        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun getLists(){
        val sh = getPreferences(Context.MODE_APPEND)
        BreakfastList = sh.getStringSet("name", BreakfastList) as MutableSet<String>
    }

    override fun onResume() {
        super.onResume()
        getLists()
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getPreferences(Context.MODE_APPEND)
        val myEdit = sharedPreferences.edit()
        myEdit.remove("name")
        myEdit.commit()
        myEdit.putStringSet("name", BreakfastList)
        myEdit.commit()
    }
}
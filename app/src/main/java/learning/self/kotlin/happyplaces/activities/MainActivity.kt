package learning.self.kotlin.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import learning.self.kotlin.happyplaces.R
import learning.self.kotlin.happyplaces.adapters.HappyPlaceAdapter
import learning.self.kotlin.happyplaces.database.DataBaseHandler
import learning.self.kotlin.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        add_place_fab.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlaceFromDB()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList : ArrayList<HappyPlaceModel>){
        happy_places_list_rv.layoutManager = LinearLayoutManager (this)

        happy_places_list_rv.setHasFixedSize(true)
        val placesAdapter = HappyPlaceAdapter(this,happyPlaceList)
        happy_places_list_rv.adapter = placesAdapter
    }

    private fun getHappyPlaceFromDB(){
        val dbHandler = DataBaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getAllHappyPlaces()

        if(getHappyPlaceList.size > 0){
            happy_places_list_rv.visibility = View.VISIBLE
            no_happy_places_tv.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlaceList)
        }else{
            happy_places_list_rv.visibility = View.GONE
            no_happy_places_tv.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getHappyPlaceFromDB()
            }else{
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }
}


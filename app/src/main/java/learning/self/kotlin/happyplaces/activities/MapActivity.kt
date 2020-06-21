package learning.self.kotlin.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import learning.self.kotlin.happyplaces.R
import learning.self.kotlin.happyplaces.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mHappyPlaceDetail : HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                        as HappyPlaceModel
        }

        if(mHappyPlaceDetail != null){
            //setting up toolbar with title and back button
            setSupportActionBar(map_toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title
            map_toolbar.setNavigationOnClickListener { onBackPressed() }

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in Sydney and move the camera
        val happyPlacePosition = LatLng(mHappyPlaceDetail!!.latitude, mHappyPlaceDetail!!.longitude)

        googleMap.addMarker(MarkerOptions().position(happyPlacePosition).title("Marker in " + mHappyPlaceDetail!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(happyPlacePosition,15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}

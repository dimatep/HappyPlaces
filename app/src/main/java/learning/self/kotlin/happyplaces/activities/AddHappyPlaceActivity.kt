package learning.self.kotlin.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import learning.self.kotlin.happyplaces.R
import learning.self.kotlin.happyplaces.database.DataBaseHandler
import learning.self.kotlin.happyplaces.models.HappyPlaceModel
import learning.self.kotlin.happyplaces.utils.GetAdressFromLatLng
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener{

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var mSaveImageToInternalStorage : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null

    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(add_place_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //add back button to toolbar
        add_place_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,resources.getString(R.string.google_maps_api_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel

        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }

        updateDateInView() //set the date when the activity is opened

        if(mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"
             title_et.setText(mHappyPlaceDetails!!.title)
            desc_et.setText(mHappyPlaceDetails!!.description)
            date_et.setText(mHappyPlaceDetails!!.date)
            location_et.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            mSaveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            add_place_image_iv.setImageURI(mSaveImageToInternalStorage)
            save_btn.text = "UPDATE"

        }

        date_et.setOnClickListener(this)
        add_image_btn.setOnClickListener(this)
        save_btn.setOnClickListener(this)
        location_et.setOnClickListener(this)
        select_current_location_tv.setOnClickListener(this)
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation : Location = locationResult!!.lastLocation
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude

            val addressTask = GetAdressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object : GetAdressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?) {
                    location_et.setText(address)
                }

                override fun onError() {
                    Toast.makeText(this@AddHappyPlaceActivity,
                        "Error: Something went wrong",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

            addressTask.getAddress() // execute
        }
    }

    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0 //ms
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.date_et -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.add_image_btn -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")

                val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Capture photo from Camera")

                pictureDialog.setItems(pictureDialogItems){
                    _, which ->
                    when(which){
                        0 -> choosePhotoFromGalleryPermissions()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.save_btn-> {
                when { //first check if all the edit texts are full
                    title_et.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    }

                    desc_et.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }

                    location_et.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }

                    mSaveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        val newHappyPlace = HappyPlaceModel(
                            if(mHappyPlaceDetails == null ) 0 else mHappyPlaceDetails!!.id,
                            title_et.text.toString(),
                            mSaveImageToInternalStorage.toString(),
                            desc_et.text.toString(),
                            date_et.text.toString(),
                            location_et.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DataBaseHandler(this)

                        if(mHappyPlaceDetails == null){
                            val addHappyPlaceResult = dbHandler.addHappyPlace(newHappyPlace)
                            if(addHappyPlaceResult > 0){
                                setResult(Activity.RESULT_OK)
                                finish() //after adding a new place close this activity and go back to the mainactivity
                            }
                        }else {
                            val updateHappyPlaceResult = dbHandler.updateHappyPlace(newHappyPlace)
                            if (updateHappyPlaceResult > 0) {
                                setResult(Activity.RESULT_OK)
                                finish() //after adding a new place close this activity and go back to the mainactivity
                            }
                        }
                    }
                }
            }

            R.id.location_et -> {
                try{
                    val fields = listOf(Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS)

                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            R.id.select_current_location_tv -> {
                if(!isLocationEnabled()){
                    Toast.makeText(this, "Your location provider is turned off. Please turn it on",Toast.LENGTH_SHORT).show()

                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if(report!!.areAllPermissionsGranted()){
                               requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread().check()
                }
            }
        }
    }

    // get picture from gallery and set it in the image_view
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)

                        mSaveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        add_place_image_iv.setImageBitmap(selectedImageBitmap)
                    }catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,"Failed to load the image form gallery",Toast.LENGTH_SHORT).show()
                    }
                }
            } else if(requestCode == CAMERA){
                val photo : Bitmap = data!!.extras!!.get("data") as Bitmap
                mSaveImageToInternalStorage = saveImageToInternalStorage(photo)
                add_place_image_iv.setImageBitmap(photo)
            } else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
                location_et.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }


    }
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGalleryPermissions(){
        //using dexter - third party library - for asking permissions

        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent,
                        GALLERY
                    )
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("" +
            "It looks like you have turned off permissions " +
                    "required for this feature. It can be enabled " +
                    "under the Application settings")
            .setPositiveButton("GO TO SETTINGS"){
                _, _ ->
                try{ //open application settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){
                dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy" //our date format
        val sdf = SimpleDateFormat(myFormat,Locale.getDefault()) //set format
        date_et.setText(sdf.format(cal.time).toString()) //set new text in edit_text
    }

    // SAVE THE IMAGE THAT WE TOOK WITH CAMERA TO GALLERY
    private fun saveImageToInternalStorage(bitmap: Bitmap) : Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg") //give random name to the image

        try{
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e : IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlaceImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}

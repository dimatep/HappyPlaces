package learning.self.kotlin.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

class GetAdressFromLatLng(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) : AsyncTask<Void, String, String>() {

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    override fun doInBackground(vararg params: Void?): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()

                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i))
                        .append(" ") //get all the data and set space between them
                }

                sb.deleteCharAt(sb.length - 1) // delete the last space
                return sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // else return an empty string
        return ""
    }

    override fun onPostExecute(result: String?) {
        if(result == null){
            mAddressListener.onError()
        }else{
            mAddressListener.onAddressFound(result)
        }
        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener : AddressListener){
        this.mAddressListener = addressListener
    }

    fun getAddress(){ //calling this method will execute all the process in the async task class
        execute()
    }

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }
}
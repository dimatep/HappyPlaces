package learning.self.kotlin.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.happy_place_item.view.*
import learning.self.kotlin.happyplaces.R
import learning.self.kotlin.happyplaces.activities.AddHappyPlaceActivity
import learning.self.kotlin.happyplaces.activities.MainActivity
import learning.self.kotlin.happyplaces.database.DataBaseHandler
import learning.self.kotlin.happyplaces.models.HappyPlaceModel

class HappyPlaceAdapter(
    private val context : Context,
    private var list: ArrayList<HappyPlaceModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemOnClickListener : ItemOnClickListener? = null

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater
                .from(context)
                .inflate(R.layout.happy_place_item,
                parent,
            false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val place = list[position]

        if(holder is MyViewHolder){
            holder.itemView.place_image_civ.setImageURI(Uri.parse(place.image))
            holder.itemView.title_tv.text = place.title
            holder.itemView.desc_tv.text = place.description

            holder.itemView.setOnClickListener{
                if(itemOnClickListener != null){
                    itemOnClickListener!!.onClick(position, place)
                }
            }
        }
    }

    fun notifyEditItem(activity : Activity, position: Int, requestCode : Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun notifyDeleteItem(position: Int){
        val db = DataBaseHandler(context)
        val isDeleted = db.deleteHappyPlace(list[position])

        if(isDeleted > 0){
            list.removeAt(position)
            notifyItemChanged(position)
        }
    }

    fun setOnClickListener(itemOnClickListener : ItemOnClickListener){
        this.itemOnClickListener = itemOnClickListener

    }

    interface ItemOnClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }
}
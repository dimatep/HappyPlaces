package learning.self.kotlin.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import learning.self.kotlin.happyplaces.models.HappyPlaceModel

class DataBaseHandler(context: Context) :
    SQLiteOpenHelper (context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "HappyPlacesDB"
        private val TABLE_HAPPY_PLACES = "HappyPlacesTable"

        private val COLUMN_ID = "_id"
        private val COLUMN_TITLE = "title"
        private val COLUMN_IMAGE = "image"
        private val COLUMN_DESCRIPTION = "description"
        private val COLUMN_DATE = "date"
        private val COLUMN_LOCATION = "location"
        private val COLUMN_LATITUDE = "latitude"
        private val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_HAPPY_PLACES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_IMAGE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_LATITUDE + " TEXT,"
                + COLUMN_LONGITUDE + " TEXT)")

        // CREATE TABLE history (_id INTEGER PRIMARY KEY, completed_date TEXT)
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACES")
        onCreate(db)
    }

    fun addHappyPlace(happyPlace : HappyPlaceModel) : Long{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_TITLE, happyPlace.title)
        values.put(COLUMN_IMAGE, happyPlace.image)
        values.put(COLUMN_DESCRIPTION, happyPlace.description)
        values.put(COLUMN_DATE, happyPlace.date)
        values.put(COLUMN_LOCATION, happyPlace.location)
        values.put(COLUMN_LATITUDE, happyPlace.latitude)
        values.put(COLUMN_LONGITUDE, happyPlace.longitude)

        // Inserting row - insert returning long
        val result = db.insert(TABLE_HAPPY_PLACES,null, values)
        db.close()
        return result
    }

    fun getAllHappyPlaces() : ArrayList<HappyPlaceModel>{
        val happyPlacesList = ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACES";
        val db = this.readableDatabase //make the db readable

        try{
            // get all the date rows
            val cursor = db.rawQuery(selectQuery, null)

            //run through all the rows that we have in database
            if(cursor.moveToFirst()){
                do{
                    val place = HappyPlaceModel(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
                    )

                    happyPlacesList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()

        }catch (e : SQLiteException){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList() //return empty arraylist
        }

        return happyPlacesList
    }
}
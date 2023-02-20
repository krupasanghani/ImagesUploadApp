package com.krupagajera.enggservicesinspection.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.krupagajera.enggservicesinspection.model.CaptureImageResponse;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "enggdatasave";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "imagedata";

    // below variable is for our record id column.
    private static final String IMAGE_RECORD_ID_COL = "ImageRecordID";

    // below variable is for our image file name column
    private static final String IMAGE_FILE_COL = "ImageFile";

    // below variable id for image date and time column.
    private static final String IMAGE_DATE_TIME_COL = "ImageDateTime";

    // below variable for image gps location column.
    private static final String IMAGE_GPS_COL = "ImageGPS";

    // below variable for audio file column.
    private static final String IMAGE_AUDIO_FILE_COL = "AudioFile";

    // below variable is for notes column.
    private static final String NOTES_COL = "Notes";

    // creating a constructor for our database handler.
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + IMAGE_RECORD_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IMAGE_FILE_COL + " TEXT,"
                + IMAGE_DATE_TIME_COL + " TEXT,"
                + IMAGE_GPS_COL + " TEXT,"
                + IMAGE_AUDIO_FILE_COL + " TEXT,"
                + NOTES_COL + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    // this method is use to add new course to our sqlite database.
    public void addNewImage(String imageFileName, String imageDateTime, String imageGps, String imageAudio, String notes) {

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(IMAGE_FILE_COL, imageFileName);
        values.put(IMAGE_DATE_TIME_COL, imageDateTime);
        values.put(IMAGE_GPS_COL, imageGps);
        values.put(IMAGE_AUDIO_FILE_COL, imageAudio);
        values.put(NOTES_COL, notes);

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values);

        // at last we are closing our
        // database after adding database.
        db.close();
    }

    // below is the method for deleting our course.
    public void deleteNewImage(String imageRecordID) {

        // on below line we are creating
        // a variable to write our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are calling a method to delete our
        // image and we are comparing it with our course name.
        db.delete(TABLE_NAME, "ImageFile=?", new String[]{imageRecordID});
        db.close();
    }

    public ArrayList<CaptureImageResponse> readImageList() {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to read data from database.
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // on below line we are creating a new array list.
        ArrayList<CaptureImageResponse> courseModalArrayList = new ArrayList<>();

        // moving our cursor to first position.
        if (cursorCourses.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                courseModalArrayList.add(new CaptureImageResponse(
                        cursorCourses.getString(1),
                        cursorCourses.getString(0),
                        cursorCourses.getString(2),
                        cursorCourses.getString(3),
                        cursorCourses.getString(4),
                        cursorCourses.getString(5)));
            } while (cursorCourses.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorCourses.close();
        return courseModalArrayList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

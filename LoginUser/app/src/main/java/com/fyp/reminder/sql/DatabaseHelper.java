package com.fyp.reminder.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.fyp.reminder.model.Reminder;
import com.fyp.reminder.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by delaroy on 3/27/17.
 */
public class DatabaseHelper  extends SQLiteOpenHelper {

    //Database version
    private static final int DATABASE_VERSION = 5;

    //Database name
    private static final String DATABASE_NAME = "UserManager.db";

    private static final String TABLE_USER = "user";
    private static final String TABLE_REMINDERS = "Reminders";


    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";


    public static final String COLUMN_ID = "_id";
    private static final String COLUMN_TASK_TITLE= "title";
    private static final String COLUMN_TASK_DESCR = "descr";
    private static final String COLUMN_TASK_DATE = "date";
    private static final String COLUMN_TASK_TIME = "time";
    private static final String COLUMN_USER_LOCATION = "location";
    private static final String COLUMN_REMINDER_TYPE = "type";
    private static final String COLUMN_SUB_TYPE = "sub_type";
    private static final String COLUMN_PRIORITY = "priority";
    private static final String COLUMN_REQUEST_CODE = "request_code";
    private static final String COLUMN_LOCATIONS_LIST = "locations_list";

    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT," + COLUMN_USER_PASSWORD + " TEXT" + ")";


    private String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TASK_TITLE + " TEXT NOT NULL,"
            + COLUMN_TASK_DESCR + " TEXT NOT NULL,"
            + COLUMN_TASK_DATE + " TEXT NOT NULL,"
            + COLUMN_TASK_TIME + " TEXT NOT NULL,"
            + COLUMN_USER_LOCATION + " TEXT NOT NULL,"
            + COLUMN_REMINDER_TYPE + " TEXT NOT NULL,"
            + COLUMN_PRIORITY + " TEXT NOT NULL,"
            + COLUMN_REQUEST_CODE +" INTEGER,"
            + COLUMN_SUB_TYPE + " TEXT,"
            + COLUMN_USER_ID + " INTEGER,"
            + COLUMN_LOCATIONS_LIST + " TEXT)";



    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;

    private String DROP_TABLE_REMINDERS = "DROP TABLE IF EXISTS " + TABLE_REMINDERS;


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_USER_TABLE);

        db.execSQL(CREATE_REMINDERS_TABLE);
    }

    @Override
    public  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(DROP_USER_TABLE);
        db.execSQL(DROP_TABLE_REMINDERS);
        onCreate(db);
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public int GetUserID(String emailId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int user_id = 0;
        String where = COLUMN_USER_EMAIL+" LIKE '%"+emailId+"%'";
        Cursor c = db.query(true, TABLE_USER, null,
                where, null, null, null, null, null);
        if(c.getCount()>0) {
            if (c != null && c.moveToFirst()) {
                do {
                    user_id = c.getInt(c.getColumnIndex(COLUMN_USER_ID));
                } while (c.moveToNext());
            }
            return user_id;
        }
        else
            return user_id;
    }


    public void addReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_TASK_TITLE, reminder.getTitle());
        values.put(COLUMN_TASK_DESCR, reminder.getDescr());
        values.put(COLUMN_TASK_DATE,reminder.getDate());
        values.put(COLUMN_TASK_TIME, reminder.getTime());


        // Inserting Row
        db.insert(TABLE_REMINDERS, null, values);

        db.close(); // Closing database connection

    }

    public boolean checkUser(String email){
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        if (cursorCount > 0){
            return true;
        }
        return false;
    }

    public boolean checkUser(String email, String password){
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " =?";
        String[] selectionArgs = { email, password };

        Cursor cursor = db.query(TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        if (cursorCount > 0){
            return true;
        }
        return false;
    }






    /**create record**/
    public void saveNewReminder(Reminder reminder) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TASK_TITLE, reminder.getTitle());
        values.put(COLUMN_TASK_DESCR, reminder.getDescr());
        values.put(COLUMN_TASK_DATE,reminder.getDate());
        values.put(COLUMN_TASK_TIME, reminder.getTime());
        values.put(COLUMN_USER_LOCATION, reminder.getLocation());
        values.put(COLUMN_REMINDER_TYPE, reminder.getType());
        values.put(COLUMN_PRIORITY, reminder.getPriority());
        values.put(COLUMN_REQUEST_CODE,reminder.getRequest_code());
        values.put(COLUMN_SUB_TYPE,reminder.getSub_type());
        values.put(COLUMN_USER_ID,reminder.getUser_id());
        values.put(COLUMN_LOCATIONS_LIST,reminder.getLocationsList());
        // insert
        db.insert(TABLE_REMINDERS,null, values);
        db.close();
    }





    /**Query only 1 record**/
    public Reminder getReminder(long id,int user_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT  * FROM " + TABLE_REMINDERS + " WHERE _id="+ id + " AND user_id=" + user_id;
        Cursor cursor = db.rawQuery(query, null);

        Reminder receivedReminder = new Reminder();
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            receivedReminder.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)));
            receivedReminder.setDescr(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCR)));
            receivedReminder.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DATE)));
            receivedReminder.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TIME)));
            receivedReminder.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_USER_LOCATION)));
            receivedReminder.setLocationsList(cursor.getString(cursor.getColumnIndex(COLUMN_LOCATIONS_LIST)));
        }



        return receivedReminder;


    }


    /**delete record**/
    public void deleteReminderRecord(long id,int user_id, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM "+TABLE_REMINDERS+" WHERE _id='"+id+"'" + "AND user_id=" + user_id);
        Toast.makeText(context, "Deleted successfully.", Toast.LENGTH_SHORT).show();

    }

    /**update record**/
    public void updateReminderRecord(long reminderId,int user_id, Context context, Reminder updatedreminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        //you can use the constants above instead of typing the column names
        db.execSQL("UPDATE  "+TABLE_REMINDERS+" SET title ='"+ updatedreminder.getTitle() + "', descr ='" + updatedreminder.getDescr()+ "', date ='"+ updatedreminder.getDate() + "', time ='"+ updatedreminder.getTime() + "', location ='"+ updatedreminder.getLocation() + "',priority ='"+ updatedreminder.getPriority() + "',request_code ='"+ updatedreminder.getRequest_code() + "',sub_type ='"+ updatedreminder.getSub_type() + "',locations_list ='"+updatedreminder.getLocationsList()+"'  WHERE _id='" + reminderId + "' AND user_id= '" + user_id + "'");
        Toast.makeText(context, "Updated successfully.", Toast.LENGTH_SHORT).show();


    }


    /**Query records, give options to filter results**/
    public List<Reminder> peopleList(String filter,int user_id) {
        String query;
        if(filter.equals("")){
            //regular query
            query = "SELECT  * FROM " + TABLE_REMINDERS + " WHERE "+COLUMN_USER_ID+ " = "+user_id;
        }else{
            //filter results by filter option provided
            query = "SELECT  * FROM " + TABLE_REMINDERS + " WHERE "+COLUMN_USER_ID+ " = "+user_id+" ORDER BY "+ filter;
        }

        List<Reminder> reminderLinkedList = new LinkedList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Reminder reminder;

        if (cursor.moveToFirst()) {
            do {
                reminder = new Reminder();

                reminder.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                reminder.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)));
                reminder.setDescr(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCR)));
                reminder.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DATE)));
                reminder.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TIME)));
                reminder.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_USER_LOCATION)));
                reminder.setPriority(cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)));
                reminder.setRequest_code(cursor.getInt(cursor.getColumnIndex(COLUMN_REQUEST_CODE)));
                reminder.setSub_type(cursor.getString(cursor.getColumnIndex(COLUMN_SUB_TYPE)));
                reminder.setLocationsList(cursor.getString(cursor.getColumnIndex(COLUMN_LOCATIONS_LIST)));
                reminderLinkedList.add(reminder);
            } while (cursor.moveToNext());
        }


        return reminderLinkedList;
    }






    
/*
    public List<Reminder> getAllReminder() {

      *//*  String selectQuery = "SELECT  * FROM " + TABLE_REMINDERS;

        // array of columns to fetch
        String[] columns = {
                COLUMN_TASK_TITLE,
                COLUMN_TASK_DESCR,
                COLUMN_TASK_DATE,
                COLUMN_TASK_TIME
        };
        // sorting orders
        String sortOrder =
                COLUMN_TASK_TITLE + " ASC";
        List<Reminder> reminderList = new ArrayList<Reminder>();

        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(TABLE_REMINDERS, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order


        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();

                reminder.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)));
                reminder.setDescr(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCR)));
                reminder.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DATE)));
                reminder.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TIME)));
                // Adding record to list
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // return list
        return reminderList;*//*

        final String TABLE_NAME = TABLE_REMINDERS;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor cursor      = db.rawQuery(selectQuery, null);

        List<Reminder> reminderList = new ArrayList<Reminder>();

//        String[] data      = null;

        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reminderList;

    }*/



}

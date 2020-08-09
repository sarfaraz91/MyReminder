package com.fyp.reminder.model;

import java.io.Serializable;

public class Reminder implements Serializable {

    private long id;
    private String title;
    private String descr;
    private String date;
    private  String time;
    private  String type;
    private String priority;
    private String sub_type;
    private int request_code;
    private int user_id;
    private String locationsList = "";

    public String getLocationsList() {
        return locationsList;
    }

    public void setLocationsList(String locationsList) {
        this.locationsList = locationsList;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSub_type() {
        return sub_type;
    }

    public int getRequest_code() {
        return request_code;
    }

    public void setRequest_code(int request_code) {
        this.request_code = request_code;
    }

    public void setSub_type(String sub_type) {
        this.sub_type = sub_type;
    }

    private  String location;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Reminder(String title, String descr, String date, String time, String location) {
        this.title = title;
        this.descr = descr;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public Reminder(String title, String descr, String date, String time, String location,String priority,int request_code) {
        this.title = title;
        this.descr = descr;
        this.date = date;
        this.time = time;
        this.location = location;
        this.priority = priority;
        this.request_code = request_code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /*
    public Reminder(String title, String descr, String date, String time) {
        this.title = title;
        this.descr = descr;
        this.date = date;
        this.time = time;

    }
*/

    public Reminder() {

    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}

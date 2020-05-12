package com.dgupta3.helloworld;
import java.sql.Timestamp;

public class data {
    String fname;
    String lname;

    int availability;
    String timeStamp;

    public data(String fname, String lname, int avail, String Time){
        this.fname = fname;
        this.lname = lname;
        this.availability = avail;
        this.timeStamp = Time;
    }

}

package com.example.attnd.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timetable")
public class TimeTableEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String startTime;
    public String endTime;
    public String mondayClass;
    public String tuesdayClass;
    public String wednesdayClass;
    public String thursdayClass;
    public String fridayClass;
    public String saturdayClass;
    public String teacherId;

    public TimeTableEntity() {
        this.teacherId = "";
    }

    public TimeTableEntity(String teacherId) {
        this.teacherId = teacherId;
        this.startTime = "00:00";
        this.endTime = "00:00";
        this.mondayClass = "-";
        this.tuesdayClass = "-";
        this.wednesdayClass = "-";
        this.thursdayClass = "-";
        this.fridayClass = "-";
        this.saturdayClass = "-";
    }
}

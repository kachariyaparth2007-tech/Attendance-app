package com.example.attnd.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "attendance", primaryKeys = {"date", "rollNo", "className", "teacherId"})
public class AttendanceEntity {
    @NonNull
    public String date;
    @NonNull
    public String rollNo;
    @NonNull
    public String className;
    @NonNull
    public String teacherId;
    public boolean isPresent;
    public String status;
    public String studentName;

    public AttendanceEntity() {
        this.date = "";
        this.rollNo = "";
        this.className = "";
        this.teacherId = "";
    }

    public AttendanceEntity(@NonNull String date, @NonNull String rollNo, @NonNull String className, String status, @NonNull String teacherId) {
        this.date = date;
        this.rollNo = rollNo;
        this.className = className;
        this.status = status;
        this.isPresent = "P".equals(status);
        this.teacherId = teacherId;
    }
}

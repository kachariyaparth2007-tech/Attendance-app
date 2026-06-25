package com.example.attnd.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "students", primaryKeys = {"rollNo", "className", "teacherId"})
public class StudentEntity {
    @NonNull
    public String rollNo;
    @NonNull
    public String className;
    @NonNull
    public String teacherId;
    public String name;

    public StudentEntity() {
        this.rollNo = "";
        this.className = "";
        this.teacherId = "";
        this.name = "";
    }

    public StudentEntity(@NonNull String rollNo, String name, @NonNull String className, @NonNull String teacherId) {
        this.rollNo = rollNo;
        this.name = name;
        this.className = className;
        this.teacherId = teacherId;
    }
}

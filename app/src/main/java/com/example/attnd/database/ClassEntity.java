package com.example.attnd.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "classes", primaryKeys = {"className", "teacherId"})
public class ClassEntity {
    @NonNull
    public String className;
    @NonNull
    public String teacherId;
    public String subject;

    public ClassEntity() {
        this.className = "";
        this.teacherId = "";
        this.subject = "";
    }

    public ClassEntity(@NonNull String className, @NonNull String teacherId, String subject) {
        this.className = className;
        this.teacherId = teacherId;
        this.subject = subject;
    }
}

package com.example.attnd.model;

import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes5.dex */
public class StudentSummary {
    public String name;
    public String rollNo;
    public boolean isExpanded = false;
    public int totalDays = 0;
    public int presentDays = 0;
    public List<String> absentDates = new ArrayList();

    public StudentSummary(String name, String rollNo) {
        this.name = name;
        this.rollNo = rollNo;
    }

    public int getPercentage() {
        if (this.totalDays == 0) {
            return 0;
        }
        return (this.presentDays * 100) / this.totalDays;
    }
}

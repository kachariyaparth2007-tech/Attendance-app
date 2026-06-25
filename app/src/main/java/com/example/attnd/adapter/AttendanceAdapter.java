package com.example.attnd.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.database.StudentEntity;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes7.dex */
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private List<StudentEntity> students;
    private Map<String, String> statusMap = new HashMap();
    private String defaultStatus = "P";

    public void setStudents(List<StudentEntity> students) {
        this.students = students;
        if (students != null) {
            for (StudentEntity student : students) {
                if (!this.statusMap.containsKey(student.rollNo)) {
                    this.statusMap.put(student.rollNo, this.defaultStatus);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setAttendanceData(Map<String, String> savedMap) {
        if (savedMap != null && !savedMap.isEmpty()) {
            this.statusMap = new HashMap(savedMap);
        }
    }

    public Map<String, String> getAttendanceData() {
        return this.statusMap;
    }

    public void clearData() {
        this.statusMap.clear();
        this.defaultStatus = "P";
        notifyDataSetChanged();
    }

    public void setAllStatus(boolean isPresent) {
        this.defaultStatus = isPresent ? "P" : "A";
        if (this.students != null) {
            for (StudentEntity student : this.students) {
                this.statusMap.put(student.rollNo, this.defaultStatus);
            }
            notifyDataSetChanged();
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(v);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final StudentEntity student = this.students.get(position);
        holder.tvName.setText(student.name);
        holder.tvRoll.setText(student.rollNo);
        String currentStatus = this.statusMap.getOrDefault(student.rollNo, this.defaultStatus);
        this.statusMap.put(student.rollNo, currentStatus);
        updateButtonState(holder.btnStatus, currentStatus);
        holder.btnStatus.setOnClickListener(view -> m199xafa13855(student, holder, view));
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$0$com-example-attnd-adapter-AttendanceAdapter, reason: not valid java name */
    /* synthetic */ void m199xafa13855(StudentEntity student, ViewHolder holder, View v) {
        String oldStatus = this.statusMap.getOrDefault(student.rollNo, this.defaultStatus);
        String newStatus = oldStatus.equals("P") ? "A" : "P";
        this.statusMap.put(student.rollNo, newStatus);
        updateButtonState(holder.btnStatus, newStatus);
    }

    private void updateButtonState(MaterialButton btn, String status) {
        if (status.equals("P")) {
            btn.setText("Present");
            btn.setLetterSpacing(0.0f);
            btn.setBackgroundColor(Color.parseColor("#E8F5E9"));
            btn.setTextColor(Color.parseColor("#4CAF50"));
            btn.setStrokeColorResource(android.R.color.transparent);
            return;
        }
        btn.setText("Absent");
        btn.setLetterSpacing(0.0f);
        btn.setBackgroundColor(Color.parseColor("#FFEBEE"));
        btn.setTextColor(Color.parseColor("#F44336"));
        btn.setStrokeColorResource(android.R.color.transparent);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.students != null) {
            return this.students.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btnStatus;
        TextView tvName;
        TextView tvRoll;

        ViewHolder(View iv) {
            super(iv);
            this.tvName = (TextView) iv.findViewById(R.id.tvName);
            this.tvRoll = (TextView) iv.findViewById(R.id.tvRoll);
            this.btnStatus = (MaterialButton) iv.findViewById(R.id.btnStatus);
        }
    }
}

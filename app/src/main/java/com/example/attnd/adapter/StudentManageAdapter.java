package com.example.attnd.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.database.StudentEntity;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes7.dex */
public class StudentManageAdapter extends RecyclerView.Adapter<StudentManageAdapter.ViewHolder> {
    private final OnStudentAction listener;
    private List<StudentEntity> students = new ArrayList();

    public interface OnStudentAction {
        void onDelete(StudentEntity student);

        void onEdit(StudentEntity student);
    }

    public StudentManageAdapter(OnStudentAction listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentEntity> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_manage, parent, false);
        return new ViewHolder(v);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder holder, int position) {
        final StudentEntity student = this.students.get(position);
        holder.tvRoll.setText(student.rollNo);
        holder.tvName.setText(student.name);
        holder.btnEdit.setOnClickListener(view -> m204xd1042d20(student, view));
        holder.btnDelete.setOnClickListener(view -> m205xaceceff(student, view));
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$0$com-example-attnd-adapter-StudentManageAdapter, reason: not valid java name */
    /* synthetic */ void m204xd1042d20(StudentEntity student, View v) {
        this.listener.onEdit(student);
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$1$com-example-attnd-adapter-StudentManageAdapter, reason: not valid java name */
    /* synthetic */ void m205xaceceff(StudentEntity student, View v) {
        this.listener.onDelete(student);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView btnDelete;
        ImageView btnEdit;
        TextView tvName;
        TextView tvRoll;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvRoll = (TextView) itemView.findViewById(R.id.tvRoll);
            this.tvName = (TextView) itemView.findViewById(R.id.tvName);
            this.btnEdit = (ImageView) itemView.findViewById(R.id.btnEdit);
            this.btnDelete = (ImageView) itemView.findViewById(R.id.btnDelete);
        }
    }
}

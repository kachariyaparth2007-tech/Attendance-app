package com.example.attnd.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.database.AttendanceEntity;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes7.dex */
public class DailyReportAdapter extends RecyclerView.Adapter<DailyReportAdapter.ViewHolder> {
    private OnItemClickListener listener;
    private List<AttendanceEntity> list = new ArrayList();
    private boolean isEditMode = false;

    public interface OnItemClickListener {
        void onItemClick(int position, AttendanceEntity item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<AttendanceEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_daily, parent, false);
        return new ViewHolder(v);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.list.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final AttendanceEntity item = this.list.get(position);
        holder.tvRoll.setText(item.rollNo);
        holder.tvName.setText(item.studentName);
        if (item.isPresent) {
            holder.tvStatus.setText("Present");
            holder.tvStatus.setTextColor(-15378652);
            holder.imgStatus.setColorFilter(-15378652);
            holder.imgStatus.setImageResource(R.drawable.present);
            holder.statusContainer.getBackground().setTint(-2822694);
        } else {
            holder.tvStatus.setText("Absent");
            holder.tvStatus.setTextColor(-9298908);
            holder.imgStatus.setColorFilter(-9298908);
            holder.statusContainer.getBackground().setTint(-469030);
            holder.imgStatus.setImageResource(R.drawable.absent);
        }
        if (this.isEditMode) {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setOnClickListener(view -> m203x6d2e393(position, item, view));
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setOnClickListener(null);
        }
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$0$com-example-attnd-adapter-DailyReportAdapter, reason: not valid java name */
    /* synthetic */ void m203x6d2e393(int position, AttendanceEntity item, View v) {
        if (this.listener != null) {
            this.listener.onItemClick(position, item);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStatus;
        View statusContainer;
        TextView tvName;
        TextView tvRoll;
        TextView tvStatus;

        public ViewHolder(View v) {
            super(v);
            this.tvRoll = (TextView) v.findViewById(R.id.tvRoll);
            this.tvName = (TextView) v.findViewById(R.id.tvName);
            this.tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            this.imgStatus = (ImageView) v.findViewById(R.id.imgStatus);
            this.statusContainer = v.findViewById(R.id.statusContainer);
        }
    }
}

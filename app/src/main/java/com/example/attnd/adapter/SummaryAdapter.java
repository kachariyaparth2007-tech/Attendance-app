package com.example.attnd.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.model.StudentSummary;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes7.dex */
public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
    private List<StudentSummary> list = new ArrayList();

    public void setList(List<StudentSummary> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_summary, parent, false);
        return new ViewHolder(v);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder holder, int position) {
        StudentSummary item = this.list.get(position);
        holder.tvRoll.setText(item.rollNo != null ? item.rollNo : "-");
        holder.tvName.setText(item.name != null ? item.name : "Unknown");
        holder.tvPresent.setText(String.valueOf(item.presentDays));
        holder.tvAbsent.setText(String.valueOf(item.totalDays - item.presentDays));
        int percent = item.getPercentage();
        holder.tvPercent.setText(percent + "%");
        if (percent < 50) {
            holder.tvPercent.setTextColor(-769226);
        } else if (percent >= 75) {
            holder.tvPercent.setTextColor(-11751600);
        } else {
            holder.tvPercent.setTextColor(-26624);
        }
        int bg = position % 2 == 0 ? -1 : -525828;
        holder.itemView.setBackgroundColor(bg);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAbsent;
        TextView tvName;
        TextView tvPercent;
        TextView tvPresent;
        TextView tvRoll;

        public ViewHolder(View v) {
            super(v);
            this.tvRoll = (TextView) v.findViewById(R.id.tvRoll);
            this.tvName = (TextView) v.findViewById(R.id.tvName);
            this.tvPresent = (TextView) v.findViewById(R.id.tvPresent);
            this.tvAbsent = (TextView) v.findViewById(R.id.tvAbsent);
            this.tvPercent = (TextView) v.findViewById(R.id.tvPercent);
        }
    }
}

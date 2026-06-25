package com.example.attnd.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.database.ClassEntity;
import java.util.List;

/* JADX INFO: loaded from: classes7.dex */
public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {
    private List<ClassEntity> classes;
    private final OnClassActionListener listener;

    public interface OnClassActionListener {
        void onClick(String className);

        void onDelete(String className);

        void onRename(String oldName);
    }

    public ClassAdapter(OnClassActionListener listener) {
        this.listener = listener;
    }

    public void setClasses(List<ClassEntity> classes) {
        this.classes = classes;
        notifyDataSetChanged();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ViewHolder(v);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ClassEntity item = this.classes.get(position);
        holder.tvName.setText(item.className);
        holder.tvSubject.setText(item.subject != null ? item.subject : "");
        holder.itemView.setOnClickListener(view -> m200lambda$onBindViewHolder$0$comexampleattndadapterClassAdapter(item, view));
        holder.btnDelete.setOnClickListener(view -> m201lambda$onBindViewHolder$1$comexampleattndadapterClassAdapter(item, view));
        holder.btnEdit.setOnClickListener(view -> m202lambda$onBindViewHolder$2$comexampleattndadapterClassAdapter(item, view));
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$0$com-example-attnd-adapter-ClassAdapter, reason: not valid java name */
    /* synthetic */ void m200lambda$onBindViewHolder$0$comexampleattndadapterClassAdapter(ClassEntity item, View v) {
        this.listener.onClick(item.className);
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$1$com-example-attnd-adapter-ClassAdapter, reason: not valid java name */
    /* synthetic */ void m201lambda$onBindViewHolder$1$comexampleattndadapterClassAdapter(ClassEntity item, View v) {
        this.listener.onDelete(item.className);
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$2$com-example-attnd-adapter-ClassAdapter, reason: not valid java name */
    /* synthetic */ void m202lambda$onBindViewHolder$2$comexampleattndadapterClassAdapter(ClassEntity item, View v) {
        this.listener.onRename(item.className);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.classes == null) {
            return 0;
        }
        return this.classes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnDelete;
        ImageButton btnEdit;
        TextView tvName;
        TextView tvSubject;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvName = (TextView) itemView.findViewById(R.id.tvClassName);
            this.tvSubject = (TextView) itemView.findViewById(R.id.tvSubject);
            this.btnDelete = (ImageButton) itemView.findViewById(R.id.btnDelete);
            this.btnEdit = (ImageButton) itemView.findViewById(R.id.btnEdit);
        }
    }
}

package com.example.attnd;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes4.dex */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {
    private Context context;
    private List<File> fileList;
    private OnFileItemListener listener;
    private List<File> selectedFiles = new ArrayList();
    private boolean isSelectionMode = false;

    public interface OnFileItemListener {
        void onFileClicked(File file);

        void onSelectionChanged(int count);
    }

    public FilesAdapter(Context context, List<File> fileList, OnFileItemListener listener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }

    public void clearSelection() {
        this.selectedFiles.clear();
        this.isSelectionMode = false;
        notifyDataSetChanged();
        this.listener.onSelectionChanged(0);
    }

    public List<File> getSelectedFiles() {
        return this.selectedFiles;
    }

    private void toggleSelection(File file) {
        if (this.selectedFiles.contains(file)) {
            this.selectedFiles.remove(file);
        } else {
            this.selectedFiles.add(file);
        }
        notifyDataSetChanged();
        if (this.selectedFiles.isEmpty()) {
            this.isSelectionMode = false;
        }
        this.listener.onSelectionChanged(this.selectedFiles.size());
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(FileViewHolder holder, int position) {
        final File file = this.fileList.get(position);
        holder.fileName.setText(file.getName());
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            holder.fileIcon.setImageResource(R.drawable.file_pdf);
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv")) {
            holder.fileIcon.setImageResource(R.drawable.file_excel);
        } else {
            holder.fileIcon.setImageResource(R.drawable.my_file);
        }
        Date lastModDate = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy | hh:mm a", Locale.getDefault());
        holder.fileDate.setText(formatter.format(lastModDate));
        boolean isSelected = this.selectedFiles.contains(file);
        if (isSelected) {
            holder.rootLayout.setBackgroundColor(0);
        } else {
            holder.rootLayout.setBackgroundColor(0);
        }
        if (this.isSelectionMode) {
            holder.fileCheckbox.setVisibility(0);
            holder.fileCheckbox.setChecked(isSelected);
            
            TypedValue typedValue = new TypedValue();
            int color = Color.parseColor("#0c56cd");
            if (this.context.getTheme().resolveAttribute(R.attr.appPrimaryColor, typedValue, true)) {
                color = typedValue.data;
            }
            CompoundButtonCompat.setButtonTintList(holder.fileCheckbox, ColorStateList.valueOf(color));
        } else {
            holder.fileCheckbox.setVisibility(8);
            holder.fileCheckbox.setChecked(false);
        }
        holder.fileCheckbox.setOnClickListener(view -> m181lambda$onBindViewHolder$0$comexampleattndFilesAdapter(file, view));
        holder.itemView.setOnLongClickListener(view -> m182lambda$onBindViewHolder$1$comexampleattndFilesAdapter(file, view));
        holder.itemView.setOnClickListener(view -> m183lambda$onBindViewHolder$2$comexampleattndFilesAdapter(file, view));
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$0$com-example-attnd-FilesAdapter, reason: not valid java name */
    /* synthetic */ void m181lambda$onBindViewHolder$0$comexampleattndFilesAdapter(File file, View v) {
        toggleSelection(file);
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$1$com-example-attnd-FilesAdapter, reason: not valid java name */
    /* synthetic */ boolean m182lambda$onBindViewHolder$1$comexampleattndFilesAdapter(File file, View v) {
        if (!this.isSelectionMode) {
            this.isSelectionMode = true;
            toggleSelection(file);
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: lambda$onBindViewHolder$2$com-example-attnd-FilesAdapter, reason: not valid java name */
    /* synthetic */ void m183lambda$onBindViewHolder$2$comexampleattndFilesAdapter(File file, View v) {
        if (!this.isSelectionMode) {
            this.listener.onFileClicked(file);
        } else {
            toggleSelection(file);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        CheckBox fileCheckbox;
        TextView fileDate;
        ImageView fileIcon;
        TextView fileName;
        ConstraintLayout rootLayout;

        public FileViewHolder(View itemView) {
            super(itemView);
            this.fileName = (TextView) itemView.findViewById(R.id.fileName);
            this.fileDate = (TextView) itemView.findViewById(R.id.fileDate);
            this.fileIcon = (ImageView) itemView.findViewById(R.id.fileicon);
            this.rootLayout = (ConstraintLayout) itemView.findViewById(R.id.rootLayout);
            this.fileCheckbox = (CheckBox) itemView.findViewById(R.id.fileCheckbox);
        }
    }
}

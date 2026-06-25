package com.example.attnd;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.FilesAdapter;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes4.dex */
public class FilesFragment extends Fragment implements FilesAdapter.OnFileItemListener {
    private FilesAdapter adapter;
    private ImageView btnClose;
    private ImageView btnDelete;
    private ImageView btnRename;
    private ImageView btnShare;
    private List<File> fileList;
    private LinearLayout headerAction;
    private ConstraintLayout headerNormal;
    private RecyclerView recyclerView;
    private TextView txtActionCount;
    private TextView txtNoFiles;
    private MainViewModel viewModel;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        this.viewModel = (MainViewModel) new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewFiles);
        this.txtNoFiles = (TextView) view.findViewById(R.id.txtNoFiles);
        this.headerNormal = (ConstraintLayout) view.findViewById(R.id.header_root);
        this.headerAction = (LinearLayout) view.findViewById(R.id.layout_action_mode);
        this.txtActionCount = (TextView) view.findViewById(R.id.action_count);
        this.btnRename = (ImageView) view.findViewById(R.id.action_rename);
        
        view.findViewById(R.id.backButton).setOnClickListener(v -> 
            requireActivity().getOnBackPressedDispatcher().onBackPressed()
        );

        this.btnDelete = (ImageView) view.findViewById(R.id.action_delete);
        this.btnShare = (ImageView) view.findViewById(R.id.action_share);
        this.btnClose = (ImageView) view.findViewById(R.id.action_close);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.fileList = new ArrayList();
        this.adapter = new FilesAdapter(getContext(), this.fileList, this);
        this.recyclerView.setAdapter(this.adapter);
        setupActionButtons();
        checkPermissionAndLoadFiles();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) { // from class: com.example.attnd.FilesFragment.1
            @Override // androidx.activity.OnBackPressedCallback
            public void handleOnBackPressed() {
                if (FilesFragment.this.adapter != null && FilesFragment.this.adapter.getSelectedFiles().size() > 0) {
                    FilesFragment.this.adapter.clearSelection();
                    return;
                }
                setEnabled(false);
                FilesFragment.this.requireActivity().getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
        return view;
    }

    private void setupActionButtons() {
        if (this.btnClose != null) {
            this.btnClose.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FilesFragment.this.m185lambda$setupActionButtons$0$comexampleattndFilesFragment(view);
                }
            });
        }
        if (this.btnDelete != null) {
            this.btnDelete.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FilesFragment.this.m187lambda$setupActionButtons$2$comexampleattndFilesFragment(view);
                }
            });
        }
        if (this.btnShare != null) {
            this.btnShare.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FilesFragment.this.m188lambda$setupActionButtons$3$comexampleattndFilesFragment(view);
                }
            });
        }
        if (this.btnRename != null) {
            this.btnRename.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FilesFragment.this.m189lambda$setupActionButtons$4$comexampleattndFilesFragment(view);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$setupActionButtons$0$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m185lambda$setupActionButtons$0$comexampleattndFilesFragment(View v) {
        this.adapter.clearSelection();
    }

    /* JADX INFO: renamed from: lambda$setupActionButtons$2$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m187lambda$setupActionButtons$2$comexampleattndFilesFragment(View v) {
        final List<File> selected = this.adapter.getSelectedFiles();
        if (selected.isEmpty()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) ("Delete " + selected.size() + " files?")).setMessage((CharSequence) "This action cannot be undone.").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded)).setPositiveButton((CharSequence) "Delete", new DialogInterface.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                FilesFragment.this.m186lambda$setupActionButtons$1$comexampleattndFilesFragment(selected, dialogInterface, i);
            }
        }).setNegativeButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$setupActionButtons$1$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m186lambda$setupActionButtons$1$comexampleattndFilesFragment(List selected, DialogInterface dialog, int which) {
        Iterator it = selected.iterator();
        while (it.hasNext()) {
            File f = (File) it.next();
            f.delete();
        }
        this.adapter.clearSelection();
        loadFiles();
        Toast.makeText(getContext(), "Deleted successfully", 0).show();
    }

    /* JADX INFO: renamed from: lambda$setupActionButtons$3$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m188lambda$setupActionButtons$3$comexampleattndFilesFragment(View v) {
        List<File> selected = this.adapter.getSelectedFiles();
        if (!selected.isEmpty()) {
            shareFiles(selected);
        }
    }

    /* JADX INFO: renamed from: lambda$setupActionButtons$4$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m189lambda$setupActionButtons$4$comexampleattndFilesFragment(View v) {
        List<File> selected = this.adapter.getSelectedFiles();
        if (selected.size() == 1) {
            showRenameDialog(selected.get(0));
        }
    }

    @Override // com.example.attnd.FilesAdapter.OnFileItemListener
    public void onFileClicked(File file) {
        openFile(file);
    }

    @Override // com.example.attnd.FilesAdapter.OnFileItemListener
    public void onSelectionChanged(int count) {
        if (this.headerNormal == null || this.headerAction == null) {
            return;
        }
        if (count > 0) {
            this.headerNormal.setVisibility(8);
            this.headerAction.setVisibility(0);
            this.txtActionCount.setText(count + " Selected");
            if (this.btnRename != null) {
                if (count > 1) {
                    this.btnRename.setAlpha(0.5f);
                    this.btnRename.setEnabled(false);
                    return;
                } else {
                    this.btnRename.setAlpha(1.0f);
                    this.btnRename.setEnabled(true);
                    return;
                }
            }
            return;
        }
        this.headerNormal.setVisibility(0);
        this.headerAction.setVisibility(8);
    }

    private void openFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            String name = file.getName().toLowerCase();
            String mimeType = "*/*";
            if (name.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (name.endsWith(".csv")) {
                mimeType = "text/csv";
            } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                mimeType = "application/vnd.ms-excel";
            }
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(1);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Cannot open file. Install a reader app.", 0).show();
        }
    }

    private void shareFiles(List<File> files) {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            for (File file : files) {
                uris.add(FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file));
            }
            Intent intent = new Intent();
            if (files.size() == 1) {
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.STREAM", uris.get(0));
            } else {
                intent.setAction("android.intent.action.SEND_MULTIPLE");
                intent.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
            }
            intent.setType("*/*");
            intent.addFlags(1);
            startActivity(Intent.createChooser(intent, "Share Files"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error sharing files", 0).show();
        }
    }

    private void showRenameDialog(final File file) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded));
        builder.setTitle((CharSequence) "Rename File");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.jayho_savefile, (ViewGroup) null);
        final TextInputEditText input = (TextInputEditText) view.findViewById(R.id.etClassName);
        input.setText(file.getName());
        input.setSelection(file.getName().length());
        builder.setView(view);
        builder.setPositiveButton((CharSequence) "RENAME", (DialogInterface.OnClickListener) null);
        builder.setNegativeButton((CharSequence) "CANCEL", new DialogInterface.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(4);
        dialog.show();
        dialog.getButton(-1).setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.FilesFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                FilesFragment.this.m190lambda$showRenameDialog$6$comexampleattndFilesFragment(input, file, dialog, view2);
            }
        });
        input.requestFocus();
    }

    /* JADX INFO: renamed from: lambda$showRenameDialog$6$com-example-attnd-FilesFragment, reason: not valid java name */
    /* synthetic */ void m190lambda$showRenameDialog$6$comexampleattndFilesFragment(TextInputEditText input, File file, AlertDialog dialog, View v) {
        String newName = input.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", 0).show();
            return;
        }
        File newFile = new File(file.getParent(), newName);
        if (file.renameTo(newFile)) {
            this.adapter.clearSelection();
            loadFiles();
            Toast.makeText(getContext(), "Renamed", 0).show();
            dialog.dismiss();
            return;
        }
        Toast.makeText(getContext(), "Rename failed", 0).show();
    }

    private void checkPermissionAndLoadFiles() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (Environment.isExternalStorageManager()) {
                loadFiles();
                return;
            }
            try {
                Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                return;
            } catch (Exception e) {
                startActivity(new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"));
                return;
            }
        }
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 100);
        } else {
            loadFiles();
        }
    }

    private void loadFiles() {
        File[] files;
        this.fileList.clear();
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File attndDir = new File(documentsDir, "ATTND");
        if (attndDir.exists() && attndDir.isDirectory() && (files = attndDir.listFiles()) != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".csv") || name.endsWith(".pdf") || name.endsWith(".xls") || name.endsWith(".xlsx")) {
                    this.fileList.add(file);
                }
            }
        }
        if (this.fileList.isEmpty()) {
            this.txtNoFiles.setVisibility(0);
            this.recyclerView.setVisibility(8);
        } else {
            this.txtNoFiles.setVisibility(8);
            this.recyclerView.setVisibility(0);
            this.adapter.notifyDataSetChanged();
        }
    }
}

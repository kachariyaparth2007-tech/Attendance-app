package com.example.attnd.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.ClassWidgetProvider;
import com.example.attnd.R;
import com.example.attnd.adapter.ClassAdapter;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ClassFragment extends Fragment implements ClassAdapter.OnClassActionListener {
    private ClassAdapter adapter;
    private List<ClassEntity> currentClasses = new ArrayList<>();
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class, container, false);
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        RecyclerView rv = view.findViewById(R.id.rvClasses);
        View llEmpty = view.findViewById(R.id.llEmptyState);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter = new ClassAdapter(this);
        rv.setAdapter(this.adapter);

        this.viewModel.getAllClasses().observe(getViewLifecycleOwner(), classes -> {
            this.currentClasses = classes;
            this.adapter.setClasses(classes);
            if (classes == null || classes.isEmpty()) {
                rv.setVisibility(View.GONE);
                if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
            } else {
                rv.setVisibility(View.VISIBLE);
                if (llEmpty != null) llEmpty.setVisibility(View.GONE);
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fabAddClass);
        fab.setOnClickListener(v -> showClassDialog(null));

        View btnTimeTable = view.findViewById(R.id.btnTimeTable);
        if (btnTimeTable != null) {
            btnTimeTable.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_time_table));
        }
        return view;
    }

    @Override
    public void onClick(String className) {
        try {
            final View view = getView();
            if (view == null) return;
            final Bundle bundle = new Bundle();
            bundle.putString("className", className);
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    NavController navController = Navigation.findNavController(view);
                    if (navController.getCurrentDestination() != null) {
                        navController.navigate(R.id.nav_student_manage, bundle);
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Nav Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Click Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showClassDialog(final String oldName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded));
        builder.setTitle(oldName == null ? "Add Class" : "Rename Class");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_class, null);
        final TextInputEditText inputName = view.findViewById(R.id.etClassName);
        final TextInputEditText inputSubject = view.findViewById(R.id.etSubject);
        
        if (oldName != null) {
            inputName.setText(oldName);
            inputName.setSelection(oldName.length());
            // Find current subject for oldName
            for (ClassEntity c : this.currentClasses) {
                if (c.className.equalsIgnoreCase(oldName)) {
                    inputSubject.setText(c.subject);
                    break;
                }
            }
        }
        
        builder.setView(view);
        builder.setPositiveButton("SAVE", null);
        builder.setNegativeButton("CANCEL", (dialog, i) -> dialog.cancel());
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String rawName = inputName.getText() != null ? inputName.getText().toString().trim() : "";
            String rawSubject = inputSubject.getText() != null ? inputSubject.getText().toString().trim() : "";
            
            String newName = rawName.toUpperCase();
            String newSubject = rawSubject.toUpperCase();
            
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newSubject.isEmpty()) {
                Toast.makeText(getContext(), "Subject cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean exists = false;
            for (ClassEntity c : this.currentClasses) {
                if (oldName == null || !c.className.equalsIgnoreCase(oldName)) {
                    if (c.className.equalsIgnoreCase(newName)) {
                        exists = true;
                        break;
                    }
                }
            }
            if (exists) {
                Toast.makeText(getContext(), "Class name already exists!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (oldName == null) {
                this.viewModel.addClass(newName, newSubject);
            } else {
                this.viewModel.renameClass(oldName, newName, newSubject);
                if (!oldName.equalsIgnoreCase(newName)) {
                    ClassWidgetProvider.onClassRenamed(requireContext(), oldName, newName);
                }
            }
            dialog.dismiss();
        });
        inputName.requestFocus();
    }

    @Override
    public void onDelete(final String className) {
        new MaterialAlertDialogBuilder(requireContext())
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded))
                .setTitle("Delete Class?")
                .setMessage("Delete " + className + "? All data will be lost.")
                .setPositiveButton("Delete", (dialog, i) -> this.viewModel.deleteClass(className))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRename(String oldName) {
        showClassDialog(oldName);
    }
}

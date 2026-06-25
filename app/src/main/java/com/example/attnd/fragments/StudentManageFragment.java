package com.example.attnd.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.adapter.StudentManageAdapter;
import com.example.attnd.database.StudentEntity;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class StudentManageFragment extends Fragment implements StudentManageAdapter.OnStudentAction {
    private StudentManageAdapter adapter;
    private String className;
    private EditText etName;
    private EditText etRoll;
    private MainViewModel viewModel;
    private List<StudentEntity> currentStudentList = new ArrayList<>();
    
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    processFile(result.getData().getData());
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_manage, container, false);
        try {
            if (getArguments() != null) {
                this.className = getArguments().getString("className");
            }
            if (this.className == null) {
                this.className = "Unknown Class";
            }
            this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
            TextView header = view.findViewById(R.id.tvHeaderTitle);
            if (header != null) {
                header.setText("Add Student");
            }
            TextView subtitle = view.findViewById(R.id.tvSubtitle);
            if (subtitle != null) {
                subtitle.setText(this.className);
            }
            this.etRoll = view.findViewById(R.id.etRollNo);
            this.etName = view.findViewById(R.id.etStudentName);
            this.etName.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == 6 || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    addManualStudent();
                    return true;
                }
                return false;
            });
            RecyclerView rv = view.findViewById(R.id.rvStudentList);
            if (rv != null) {
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                this.adapter = new StudentManageAdapter(this);
                rv.setAdapter(this.adapter);
            }
            this.viewModel.getStudents(this.className).observe(getViewLifecycleOwner(), students -> {
                if (students == null) {
                    students = new ArrayList<>();
                }
                this.currentStudentList = students;
                if (this.adapter != null) {
                    this.adapter.setStudents(students);
                }
            });
            View btnAdd = view.findViewById(R.id.btnAddStudent);
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> addManualStudent());
            }
            View btnImport = view.findViewById(R.id.btnImport);
            if (btnImport != null) {
                btnImport.setOnClickListener(v -> openFilePicker());
            }
            ImageView btnBack = view.findViewById(R.id.backButton);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void addManualStudent() {
        String roll = this.etRoll.getText().toString().trim();
        String name = this.etName.getText().toString().trim().toUpperCase();
        if (roll.isEmpty() || name.isEmpty()) {
            Toast.makeText(getContext(), "Enter Roll No and Name", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean exists = false;
        for (StudentEntity s : this.currentStudentList) {
            if (s.rollNo.equalsIgnoreCase(roll)) {
                exists = true;
                break;
            }
        }
        if (exists) {
            Toast.makeText(getContext(), "Roll No " + roll + " already exists in this class.", Toast.LENGTH_SHORT).show();
            return;
        }
        this.viewModel.addStudent(name, roll, this.className);
        this.etRoll.setText("");
        this.etName.setText("");
        this.etRoll.requestFocus();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/comma-separated-values", "text/csv", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        this.filePickerLauncher.launch(intent);
    }

    private void processFile(Uri uri) {
        try {
            String mimeType = requireContext().getContentResolver().getType(uri);
            String filename = getFileName(uri);
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (filename.toLowerCase().endsWith(".csv") || (mimeType != null && mimeType.contains("csv"))) {
                parseCSV(inputStream);
                Toast.makeText(getContext(), "CSV Imported Successfully!", Toast.LENGTH_SHORT).show();
            } else if (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx") || 
                    (mimeType != null && (mimeType.contains("excel") || mimeType.contains("spreadsheet")))) {
                parseExcel(inputStream);
                Toast.makeText(getContext(), "Excel Imported Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Unsupported file format: " + filename, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex("_display_name");
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void parseCSV(InputStream is) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2) {
                    String roll = tokens[0].trim().replace("\"", "");
                    String name = tokens[1].trim().replace("\"", "");
                    if (!roll.toLowerCase().contains("roll") || !name.toLowerCase().contains("name")) {
                        this.viewModel.addStudent(name, roll, this.className);
                    }
                }
            }
        }
    }

    private void parseExcel(InputStream is) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (Row row : sheet) {
                if (row.getRowNum() != 0) {
                    Cell cellRoll = row.getCell(0);
                    Cell cellName = row.getCell(1);
                    if (cellRoll != null && cellName != null) {
                        String roll = formatter.formatCellValue(cellRoll).trim();
                        String name = formatter.formatCellValue(cellName).trim();
                        if (!roll.isEmpty() && !name.isEmpty()) {
                            this.viewModel.addStudent(name, roll, this.className);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEdit(final StudentEntity student) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded));
        builder.setTitle("Edit Student");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_student, null);
        final TextInputEditText inputRoll = view.findViewById(R.id.etRollNo);
        final TextInputEditText inputName = view.findViewById(R.id.etStudentName);
        inputRoll.setText(student.rollNo);
        inputName.setText(student.name);
        builder.setView(view);
        builder.setPositiveButton("Update", null);
        builder.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newRoll = inputRoll.getText().toString().trim();
            String newName = inputName.getText().toString().trim().toUpperCase();
            if (newRoll.isEmpty() || newName.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                this.viewModel.updateStudent(student, newName, newRoll, new MainViewModel.OnUpdateListener() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
    }

    @Override
    public void onDelete(final StudentEntity student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded))
                .setTitle("Delete Student")
                .setMessage("Remove " + student.name + "?")
                .setPositiveButton("Yes", (dialog, i) -> this.viewModel.deleteStudent(student))
                .setNegativeButton("No", null)
                .show();
    }
}

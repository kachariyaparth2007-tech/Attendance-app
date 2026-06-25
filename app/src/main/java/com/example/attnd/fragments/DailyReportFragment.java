package com.example.attnd.fragments;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.adapter.DailyReportAdapter;
import com.example.attnd.database.AttendanceEntity;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class DailyReportFragment extends Fragment {
    private DailyReportAdapter adapter;
    private ImageButton btnDownload;
    private Button btnUpdateSave;
    private String className;
    private String selectedDate;
    private TextView tvDate;
    private TextView tvNoRecords;
    private MainViewModel viewModel;
    private boolean isEditMode = false;
    private List<AttendanceEntity> originalList = new ArrayList<>();
    private List<AttendanceEntity> workingList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_report, container, false);
        if (getArguments() != null) {
            this.className = getArguments().getString("className");
        }
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        this.tvDate = view.findViewById(R.id.tvDateSelector);
        this.tvNoRecords = view.findViewById(R.id.tvNoRecords);
        this.btnUpdateSave = view.findViewById(R.id.btnUpdateSave);
        this.btnDownload = view.findViewById(R.id.save_daily);

        TextView subtitle = view.findViewById(R.id.tvSubtitle);
        if (subtitle != null) {
            subtitle.setText(this.className);
        }

        RecyclerView rv = view.findViewById(R.id.rvDailyReport);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter = new DailyReportAdapter();
        rv.setAdapter(this.adapter);

        Calendar cal = Calendar.getInstance();
        updateDateLabel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        this.tvDate.setOnClickListener(v -> {
            if (this.isEditMode) {
                exitEditMode();
            } else {
                showDatePicker();
            }
        });

        this.btnUpdateSave.setOnClickListener(v -> {
            if (!this.isEditMode) {
                enterEditMode();
            } else {
                saveChanges();
            }
        });

        this.btnDownload.setOnClickListener(v -> showExportDialog(this.className));

        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        this.adapter.setOnItemClickListener((position, item) -> {
            if (this.isEditMode) {
                item.isPresent = !item.isPresent;
                item.status = item.isPresent ? "P" : "A";
                this.adapter.notifyItemChanged(position);
                checkIfDataChanged();
            }
        });
        return view;
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        try {
            String[] parts = this.selectedDate.split("-");
            c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception ignored) {}

        new DatePickerDialog(requireContext(), (view1, year, month, day) -> updateDateLabel(year, month, day),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel(int year, int month, int day) {
        this.selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
        this.tvDate.setText(this.selectedDate);
        loadData(this.selectedDate);
    }

    private void loadData(String date) {
        this.isEditMode = false;
        resetUI();
        this.viewModel.getDailyAttendance(this.className, date).observe(getViewLifecycleOwner(), list -> {

            // --- NEW SORTING LOGIC TO FIX THE 1-60 vs 121-160 BUG ---
            if (list != null && !list.isEmpty()) {
                Collections.sort(list, (a1, a2) -> {
                    try {
                        // Parse roll numbers as real Integers so 2 comes before 10
                        int r1 = Integer.parseInt(a1.rollNo.trim());
                        int r2 = Integer.parseInt(a2.rollNo.trim());
                        return Integer.compare(r1, r2);
                    } catch (NumberFormatException e) {
                        // Fallback: If a roll number has letters (e.g., "12A"), sort it alphabetically
                        return a1.rollNo.compareToIgnoreCase(a2.rollNo);
                    }

                    // NOTE: If you truly wanted to sort strictly by Student Name instead of Roll Number,
                    // delete the try/catch block above and use this single line instead:
                    // return a1.studentName.compareToIgnoreCase(a2.studentName);
                });
            }
            // --------------------------------------------------------

            this.originalList = new ArrayList<>();
            this.workingList = new ArrayList<>();
            for (AttendanceEntity a : list) {
                AttendanceEntity copyOrig = new AttendanceEntity(a.date, a.rollNo, a.className, a.status, a.teacherId);
                copyOrig.studentName = a.studentName;
                this.originalList.add(copyOrig);

                AttendanceEntity copyWork = new AttendanceEntity(a.date, a.rollNo, a.className, a.status, a.teacherId);
                copyWork.studentName = a.studentName;
                this.workingList.add(copyWork);
            }

            if (this.workingList.isEmpty()) {
                this.tvNoRecords.setVisibility(View.VISIBLE);
                this.btnUpdateSave.setVisibility(View.GONE);
                this.btnDownload.setVisibility(View.GONE);
                setWeight(this.tvDate, 2.0f);
                this.adapter.setList(new ArrayList<>());
                return;
            }

            this.tvNoRecords.setVisibility(View.GONE);
            this.btnUpdateSave.setVisibility(View.VISIBLE);
            this.btnDownload.setVisibility(View.VISIBLE);
            this.btnUpdateSave.setText("Update");
            setWeight(this.tvDate, 1.0f);
            setWeight(this.btnUpdateSave, 1.0f);
            this.adapter.setList(this.workingList);
        });
    }

    private void enterEditMode() {
        this.isEditMode = true;
        this.adapter.setEditMode(true);
        this.btnDownload.setVisibility(View.GONE);
        this.tvDate.setText("Cancel");
        this.tvDate.setBackgroundResource(R.drawable.bg_capsule);
        this.tvDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.btnUpdateSave.setText("Save");
        this.btnUpdateSave.setEnabled(false);
        this.btnUpdateSave.setBackgroundColor(Color.LTGRAY);
    }

    private void exitEditMode() {
        this.isEditMode = false;
        this.adapter.setEditMode(false);
        this.workingList.clear();
        for (AttendanceEntity a : this.originalList) {
            AttendanceEntity copyWork = new AttendanceEntity(a.date, a.rollNo, a.className, a.status, a.teacherId);
            copyWork.studentName = a.studentName;
            this.workingList.add(copyWork);
        }
        this.adapter.setList(this.workingList);
        resetUI();
        if (!this.workingList.isEmpty()) {
            this.btnDownload.setVisibility(View.VISIBLE);
        }
    }

    private void resetUI() {
        this.tvDate.setText(this.selectedDate);
        this.tvDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        this.tvDate.setBackgroundResource(R.drawable.bg_capsule);
        this.btnUpdateSave.setText("Update");
        this.btnUpdateSave.setEnabled(true);
        this.btnUpdateSave.setBackgroundColor(getThemeColor(R.attr.appPrimaryColor));
    }

    private void checkIfDataChanged() {
        boolean hasChanged = false;
        if (this.workingList.size() == this.originalList.size()) {
            for (int i = 0; i < this.workingList.size(); i++) {
                if (this.workingList.get(i).isPresent != this.originalList.get(i).isPresent) {
                    hasChanged = true;
                    break;
                }
            }
        }
        if (hasChanged) {
            this.btnUpdateSave.setEnabled(true);
            this.btnUpdateSave.setBackgroundColor(getThemeColor(R.attr.appPrimaryColor));
        } else {
            this.btnUpdateSave.setEnabled(false);
            this.btnUpdateSave.setBackgroundColor(Color.LTGRAY);
        }
    }

    private int getThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        if (getContext() != null && getContext().getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return Color.BLUE;
    }

    private void saveChanges() {
        int changesCount = 0;
        List<AttendanceEntity> updates = new ArrayList<>();
        for (int i = 0; i < this.workingList.size(); i++) {
            if (this.workingList.get(i).isPresent != this.originalList.get(i).isPresent) {
                changesCount++;
            }
            updates.add(this.workingList.get(i));
        }
        if (changesCount > 0) {
            this.viewModel.saveAttendance(updates);
            Toast.makeText(getContext(), changesCount + " updated", Toast.LENGTH_SHORT).show();
            this.originalList.clear();
            for (AttendanceEntity a : this.workingList) {
                AttendanceEntity copy = new AttendanceEntity(a.date, a.rollNo, a.className, a.status, a.teacherId);
                copy.studentName = a.studentName;
                this.originalList.add(copy);
            }
            this.isEditMode = false;
            this.adapter.setEditMode(false);
            resetUI();
            this.btnDownload.setVisibility(View.VISIBLE);
        }
    }

    private void setWeight(View view, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }

    private void showExportDialog(final String classname) {
        if (Build.VERSION.SDK_INT < 29 && ContextCompat.checkSelfPermission(requireContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 101);
            return;
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Export Attendance");
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded));
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export, null);
        final TextInputEditText inputFilename = view.findViewById(R.id.etFilename);
        final RadioGroup rg = view.findViewById(R.id.rgExportType);

        String autoName = "Report";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("ddMMM", Locale.US);
            autoName = outFormat.format(inFormat.parse(this.selectedDate)).toUpperCase();
        } catch (Exception ignored) {}

        inputFilename.setText(classname + "_" + autoName);
        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputFilename.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Filename cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                boolean isExcel = rg.getCheckedRadioButtonId() == R.id.rbXls;
                saveDirectlyToStorage(name, isExcel);
            }
        });
        builder.setNeutralButton("Copy", (dialog, which) -> {
            copyAttendanceToClipboard(classname);
            Toast.makeText(getContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void copyAttendanceToClipboard(String classname) {
        StringBuilder sbPresent = new StringBuilder();
        StringBuilder sbAbsent = new StringBuilder();
        int pCount = 0;
        int aCount = 0;
        for (AttendanceEntity item : this.workingList) {
            if (item.isPresent) {
                pCount++;
                if (sbPresent.length() > 0) sbPresent.append(", ");
                sbPresent.append(item.rollNo);
            } else {
                aCount++;
                if (sbAbsent.length() > 0) sbAbsent.append(", ");
                sbAbsent.append(item.rollNo);
            }
        }
        String copyText = String.format(Locale.US, "%s\n\nPresent Numbers (%d): \n %s \n\n Absent Numbers (%d): \n %s\n ", classname, pCount, sbPresent.toString(), aCount, sbAbsent.toString());
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Attendance", copyText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    private void saveDirectlyToStorage(String filename, boolean isExcel) {
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDir = new File(documentsDir, "Attnd");
        if (!appDir.exists()) {
            if (!appDir.mkdirs() && !appDir.exists()) {
                Toast.makeText(getContext(), "Failed to create directory in Documents", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String extension = isExcel ? ".xls" : ".pdf";
        File file = new File(appDir, filename + extension);
        if (file.exists()) {
            Toast.makeText(getContext(), "File already exists with this name!", Toast.LENGTH_SHORT).show();
        } else if (isExcel) {
            writeExcel(file);
        } else {
            writePdf(file);
        }
    }

    private void writeExcel(File file) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Roll No");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Status");
            for (int i = 0; i < this.workingList.size(); i++) {
                AttendanceEntity item = this.workingList.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.rollNo);
                row.createCell(1).setCellValue(item.studentName);
                row.createCell(2).setCellValue(item.isPresent ? "P" : "A");
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            Toast.makeText(getContext(), "Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void writePdf(File file) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(16.0f);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Daily Attendance Report: " + this.selectedDate, 40.0f, 50.0f, titlePaint);
        canvas.drawText("Class: " + this.className, 40.0f, 80.0f, titlePaint);

        int y = 130;
        try {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.attnd);
            if (drawable != null) {
                paint.setTextSize(16.0f);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                float brandTextWidth = paint.measureText("ATTND");
                paint.setTextSize(8.0f);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                float subTextWidth = paint.measureText("By Nirbhay D. & Parth K.");
                float maxTextWidth = Math.max(brandTextWidth, subTextWidth);
                float brandX = (575.0f - maxTextWidth) - 20.0f;
                float subTextX = (575.0f - maxTextWidth) - 20.0f;
                float subTextY = 36.0f + 12.0f;

                paint.setTextSize(16.0f);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setColor(Color.BLACK);
                canvas.drawText("ATTND", brandX, 36.0f, paint);

                paint.setTextSize(8.0f);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                canvas.drawText("By Nirbhay D. & Parth K.", subTextX, subTextY, paint);

                float lineX = (575.0f - maxTextWidth) - 30.0f;
                paint.setColor(Color.argb(80, 0, 0, 0));
                paint.setStrokeWidth(1.5f);
                canvas.drawLine(lineX, 15.0f, lineX, 15.0f + 32.0f, paint);

                float logoX = (lineX - 10.0f) - 32.0f;
                drawable.setBounds((int) logoX, 15, (int) (logoX + 32.0f), (int) (32.0f + 15.0f));
                drawable.draw(canvas);
            }
        } catch (Exception ignored) {}

        drawPdfHeaders(canvas, y);
        int yPos = y + 40;
        int pageNumber = 1;

        for (AttendanceEntity item : this.workingList) {
            if (yPos > 842 - 50) {
                document.finishPage(page);
                pageNumber++;
                page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create());
                canvas = page.getCanvas();
                drawPdfHeaders(canvas, 50);
                yPos = 50 + 40;
            }
            paint.setTextSize(14.0f);
            paint.setFakeBoldText(false);
            paint.setColor(Color.BLACK);
            canvas.drawText(item.rollNo, 40.0f, yPos, paint);

            String name = item.studentName;
            if (name.length() > 32) name = name.substring(0, 32) + "....";
            canvas.drawText(name, 120.0f, yPos, paint);

            if (item.isPresent) paint.setColor(Color.parseColor("#388E3C"));
            else paint.setColor(Color.RED);

            canvas.drawText(item.isPresent ? "Present" : "Absent", 500.0f, yPos, paint);
            yPos += 30;
        }
        document.finishPage(page);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Toast.makeText(getContext(), "Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private void drawPdfHeaders(Canvas canvas, int y) {
        Paint paint = new Paint();
        paint.setTextSize(14.0f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("Roll No", 40.0f, y, paint);
        canvas.drawText("Name", 120.0f, y, paint);
        canvas.drawText("Status", 500.0f, y, paint);
        canvas.drawLine(40.0f, y + 10, 550.0f, y + 10, paint);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
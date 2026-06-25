package com.example.attnd.fragments;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attnd.R;
import com.example.attnd.adapter.SummaryAdapter;
import com.example.attnd.database.AttendanceEntity;
import com.example.attnd.database.StudentEntity;
import com.example.attnd.model.StudentSummary;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SummaryFragment extends Fragment {
    private SummaryAdapter adapter;
    private View btnExport;
    private TextView btnSort;
    private TextView tvEmpty;
    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvSubtitle;
    private MainViewModel viewModel;
    private String fromDate = null;
    private String toDate = null;
    private List<StudentSummary> currentList = new ArrayList<>();
    private List<StudentSummary> originalList = new ArrayList<>();
    private String selectedClass = null;
    private int sortState = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        // class selection spinner removed from layout; selection comes from ViewModel or navigation args
        this.tvFrom = view.findViewById(R.id.tvFromDate);
        this.tvTo = view.findViewById(R.id.tvToDate);
        this.tvEmpty = view.findViewById(R.id.tvEmptySummary);
        this.tvSubtitle = view.findViewById(R.id.tvSubtitle);
        this.btnExport = view.findViewById(R.id.btnExportSummary);
        this.btnSort = view.findViewById(R.id.btnSort);
        
        RecyclerView rv = view.findViewById(R.id.rvSummary);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter = new SummaryAdapter();
        rv.setAdapter(this.adapter);

        // No in-fragment class selector; classes are managed elsewhere

        Calendar today = Calendar.getInstance();
        Calendar first = (Calendar) today.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        this.fromDate = String.format(Locale.US, "%04d-%02d-%02d", first.get(Calendar.YEAR), first.get(Calendar.MONTH) + 1, first.get(Calendar.DAY_OF_MONTH));
        this.toDate = String.format(Locale.US, "%04d-%02d-%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
        this.tvFrom.setText(this.fromDate);
        this.tvTo.setText(this.toDate);

        // If class provided via navigation args, set it; otherwise rely on ViewModel.currentSelectedClass
        Bundle args = getArguments();
        if (args != null) {
            String argClass = args.getString("className", null);
            if (argClass != null && !argClass.isEmpty()) {
                this.viewModel.currentSelectedClass = argClass;
                this.selectedClass = argClass;
            }
        }
        if (this.viewModel.currentSelectedClass != null && !this.viewModel.currentSelectedClass.isEmpty()) {
            generateSummary();
        }

        this.tvFrom.setOnClickListener(v -> showDatePicker(true));
        this.tvTo.setOnClickListener(v -> showDatePicker(false));

        // Spinner removed: selection handled externally

        this.btnSort.setOnClickListener(v -> {
            if (this.originalList.isEmpty()) {
                Toast.makeText(getContext(), "No data to sort", Toast.LENGTH_SHORT).show();
            } else {
                this.sortState = (this.sortState + 1) % 3;
                applySorting();
            }
        });

        this.btnExport.setOnClickListener(v -> {
            if (this.selectedClass == null || this.selectedClass.isEmpty() || this.currentList.isEmpty()) {
                Toast.makeText(getContext(), "Please generate the report first", Toast.LENGTH_SHORT).show();
            } else {
                showSaveDialog(this.selectedClass + "_Summary");
            }
        });

        ImageView btnBack = view.findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void showDatePicker(final boolean isFrom) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            if (isFrom) {
                // Check if FROM date is not greater than current TO date
                if (date.compareTo(this.toDate) > 0) {
                    Toast.makeText(getContext(), "Invalid date range", Toast.LENGTH_SHORT).show();
                    return;
                }
                this.fromDate = date;
                this.tvFrom.setText(date);
            } else {
                // Check if TO date is not less than current FROM date
                if (date.compareTo(this.fromDate) < 0) {
                    Toast.makeText(getContext(), "Invalid date range", Toast.LENGTH_SHORT).show();
                    return;
                }
                this.toDate = date;
                this.tvTo.setText(date);
            }
            generateSummary();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void generateSummary() {
        // Validate dates
        if (this.fromDate == null || this.toDate == null) {
            Toast.makeText(getContext(), "Please select From and To dates", Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.fromDate.compareTo(this.toDate) > 0) {
            Toast.makeText(getContext(), "Invalid date range", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine class name: use nav arg first, then ViewModel
        String className = null;
        Bundle args = getArguments();
        if (args != null && args.getString("className") != null && !args.getString("className").isEmpty()) {
            className = args.getString("className");
        } else if (this.viewModel.currentSelectedClass != null && !this.viewModel.currentSelectedClass.isEmpty()) {
            className = this.viewModel.currentSelectedClass;
        }

        if (className == null || className.isEmpty()) {
            Toast.makeText(getContext(), "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        this.selectedClass = className;
        this.viewModel.currentSelectedClass = className;
        if (this.tvSubtitle != null) {
            this.tvSubtitle.setText(className);
        }

        // Make final copies so lambdas can capture them
        final String classNameFinal = className;
        final String fromFinal = this.fromDate;
        final String toFinal = this.toDate;

        this.viewModel.getStudents(classNameFinal).observe(getViewLifecycleOwner(), students -> {
            this.viewModel.getAttendanceInRange(classNameFinal, fromFinal, toFinal).observe(getViewLifecycleOwner(), attendance -> {
                processSummaryData(students, attendance);
            });
        });
    }

    private void processSummaryData(List<StudentEntity> students, List<AttendanceEntity> attendance) {
        List<StudentSummary> list = new ArrayList<>();
        if (students != null) {
            for (StudentEntity s : students) {
                StudentSummary ss = new StudentSummary(s.name, s.rollNo);
                ss.totalDays = 0;
                ss.presentDays = 0;
                ss.absentDates = new ArrayList<>();
                if (attendance != null) {
                    for (AttendanceEntity a : attendance) {
                        if (a.rollNo.equals(s.rollNo)) {
                            ss.totalDays++;
                            if (a.isPresent) {
                                ss.presentDays++;
                            } else {
                                ss.absentDates.add(a.date);
                            }
                        }
                    }
                }
                list.add(ss);
            }
        }
        this.originalList = new ArrayList<>(list);
        this.currentList = new ArrayList<>(list);
        this.sortState = 0;
        applySorting();
        if (this.tvEmpty != null) {
            this.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void applySorting() {
        if (this.currentList == null) return;
        if (this.sortState == 0) {
            this.currentList = new ArrayList<>(this.originalList);
            this.btnSort.setText("Sort");
            this.btnSort.setCompoundDrawablesWithIntrinsicBounds(R.drawable.my_sort, 0, 0, 0);
        } else if (this.sortState == 1) {
            this.currentList.sort((s1, s2) -> Integer.compare(s2.getPercentage(), s1.getPercentage()));
            this.btnSort.setText("Sort By: Highest %");
            this.btnSort.setCompoundDrawablesWithIntrinsicBounds(R.drawable.up_sort, 0, 0, 0);
        } else if (this.sortState == 2) {
            this.currentList.sort((s1, s2) -> Integer.compare(s1.getPercentage(), s2.getPercentage()));
            this.btnSort.setText("Sort By: Lowest %");
            this.btnSort.setCompoundDrawablesWithIntrinsicBounds(R.drawable.down_sort, 0, 0, 0);
        }
        this.adapter.setList(this.currentList);
    }

    private boolean saveSummaryToXls(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            StringBuilder xls = new StringBuilder();
            xls.append("Roll No\tName\t Present \t Absent \tPercentage\n");
            for (StudentSummary s : this.currentList) {
                xls.append(s.rollNo != null ? s.rollNo : "").append("\t")
                   .append(s.name != null ? s.name : "").append("\t")
                   .append(s.presentDays).append("\t")
                   .append(s.totalDays - s.presentDays).append("\t")
                   .append(s.getPercentage()).append("%\n");
            }
            fos.write(xls.toString().getBytes());
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveSummaryToPdf(File file) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20.0f);
        paint.setFakeBoldText(true);
        canvas.drawText("Attendance Summary: " + (this.selectedClass != null ? this.selectedClass : ""), 50.0f, 50.0f, paint);
        
        paint.setTextSize(14.0f);
        paint.setFakeBoldText(false);
        canvas.drawText("From: " + this.fromDate + " To: " + this.toDate, 50.0f, 75.0f, paint);
        
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

        paint.setTextSize(12.0f);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Roll", 40.0f, 110, paint);
        canvas.drawText("Name", 100.0f, 110, paint);
        canvas.drawText("Present", 320.0f, 110, paint);
        canvas.drawText("Absent", 380.0f, 110, paint);
        canvas.drawText("%", 480.0f, 110, paint);
        
        canvas.drawLine(40.0f, 115, 550.0f, 115, paint);
        
        int y = 135;
        paint.setFakeBoldText(false);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12.0f);
        textPaint.setAntiAlias(true);

        for (StudentSummary s : this.currentList) {
            String nameStr = s.name != null ? s.name : "-";
            StaticLayout staticLayout = new StaticLayout(nameStr, textPaint, 200, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int rowHeight = Math.max(staticLayout.getHeight(), 20);

            if (y + rowHeight > 842 - 50) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
                // Redraw headers on new page if needed
            }

            paint.setColor(Color.BLACK);
            canvas.drawText(s.rollNo != null ? s.rollNo : "-", 40.0f, y + 10, paint);
            
            canvas.save();
            canvas.translate(100.0f, y);
            staticLayout.draw(canvas);
            canvas.restore();
            
            canvas.drawText(String.valueOf(s.presentDays), 320.0f, y + 10, paint);
            canvas.drawText(String.valueOf(s.totalDays - s.presentDays), 380.0f, y + 10, paint);
            
            int percentVal = s.getPercentage();
            if (percentVal >= 75) paint.setColor(Color.parseColor("#388E3C"));
            else if (percentVal >= 50) paint.setColor(Color.parseColor("#F57C00"));
            else paint.setColor(Color.RED);
            
            canvas.drawText(percentVal + "%", 480.0f, y + 10, paint);
            
            y += rowHeight + 10;
        }
        document.finishPage(page);
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            document.close();
        }
    }

    private void showSaveDialog(String defaultName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Export Summary");
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg_rounded));
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export_summary, null);
        final TextInputEditText input = view.findViewById(R.id.etFilename);
        final RadioButton rbExcel = view.findViewById(R.id.rbExcel);
        input.setText(defaultName);
        builder.setView(view);
        builder.setPositiveButton("SAVE", null);
        builder.setNegativeButton("CANCEL", (dialog, i) -> dialog.cancel());
        
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String fileName = input.getText() != null ? input.getText().toString().trim() : "";
            if (fileName.isEmpty()) {
                Toast.makeText(getContext(), "File name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean isExcel = rbExcel.isChecked();
            String extension = isExcel ? ".xls" : ".pdf";
            if (!fileName.endsWith(extension)) fileName += extension;
            
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ATTND");
            if (!folder.exists()) folder.mkdirs();
            File file = new File(folder, fileName);
            
            if (file.exists()) {
                Toast.makeText(getContext(), "File already exists!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean success = isExcel ? saveSummaryToXls(file) : saveSummaryToPdf(file);
            if (success) {
                Toast.makeText(getContext(), "Saved: Documents/ATTND/" + file.getName(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

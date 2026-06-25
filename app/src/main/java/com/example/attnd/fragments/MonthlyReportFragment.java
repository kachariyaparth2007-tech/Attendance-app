package com.example.attnd.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.attnd.R;
import com.example.attnd.database.AttendanceEntity;
import com.example.attnd.database.StudentEntity;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/* JADX INFO: loaded from: classes9.dex */
public class MonthlyReportFragment extends Fragment {
    private final int DATE_WIDTH = 120;
    private final int ROW_HEIGHT = 120;
    private ImageButton btnExport;
    private TextView btnMonthPicker;
    private String className;
    private LinearLayout contentContainer;
    private List<AttendanceEntity> currentAttendance;
    private List<StudentEntity> currentStudents;
    private LinearLayout headerContainer;
    private HorizontalScrollView hsvContent;
    private HorizontalScrollView hsvHeader;
    private LinearLayout rollNoContainer;
    private int selectedMonth;
    private int selectedYear;
    private MainViewModel viewModel;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);
        if (getArguments() != null) {
            this.className = getArguments().getString("className");
        }
        this.viewModel = (MainViewModel) new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.headerContainer = (LinearLayout) view.findViewById(R.id.headerContainer);
        this.rollNoContainer = (LinearLayout) view.findViewById(R.id.rollNoContainer);
        this.contentContainer = (LinearLayout) view.findViewById(R.id.contentContainer);
        this.hsvHeader = (HorizontalScrollView) view.findViewById(R.id.hsvHeader);
        this.hsvContent = (HorizontalScrollView) view.findViewById(R.id.hsvContent);
        this.btnMonthPicker = (TextView) view.findViewById(R.id.btnMonthPicker);
        this.btnExport = (ImageButton) view.findViewById(R.id.btnExport);

        TextView subtitle = view.findViewById(R.id.tvSubtitle);
        if (subtitle != null) {
            subtitle.setText(this.className);
        }
        this.hsvContent.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda5
            @Override // android.view.View.OnScrollChangeListener
            public final void onScrollChange(View view2, int i, int i2, int i3, int i4) {
                MonthlyReportFragment.this.m239x159dbfc7(view2, i, i2, i3, i4);
            }
        });
        this.hsvHeader.setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda6
            @Override // android.view.View.OnScrollChangeListener
            public final void onScrollChange(View view2, int i, int i2, int i3, int i4) {
                MonthlyReportFragment.this.m240x5928dd88(view2, i, i2, i3, i4);
            }
        });
        Calendar cal = Calendar.getInstance();
        this.selectedYear = cal.get(1);
        this.selectedMonth = cal.get(2);
        updateMonthLabel();
        this.btnMonthPicker.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MonthlyReportFragment.this.m242xe03f190a(view2);
            }
        });
        this.btnExport.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MonthlyReportFragment.this.m243x23ca36cb(view2);
            }
        });
        view.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MonthlyReportFragment.this.m244x6755548c(view2);
            }
        });
        loadData();
        return view;
    }

    /* JADX INFO: renamed from: lambda$onCreateView$0$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m239x159dbfc7(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        this.hsvHeader.scrollTo(scrollX, 0);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$1$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m240x5928dd88(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        this.hsvContent.scrollTo(scrollX, 0);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$2$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m241x9cb3fb49() {
        this.hsvHeader.scrollTo(this.hsvContent.getScrollX(), 0);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$3$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m242xe03f190a(View v) {
        showMonthYearPicker();
    }

    /* JADX INFO: renamed from: lambda$onCreateView$4$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m243x23ca36cb(View v) {
        if (this.currentStudents == null || this.currentStudents.isEmpty()) {
            Toast.makeText(getContext(), "No data", 0).show();
        } else {
            showSaveDialog(this.className + "_" + (this.selectedMonth + 1) + "_" + this.selectedYear);
        }
    }

    /* JADX INFO: renamed from: lambda$onCreateView$5$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m244x6755548c(View v) {
        requireActivity().onBackPressed();
    }

    private void loadData() {
        this.viewModel.getStudents(this.className).observe(getViewLifecycleOwner(), new Observer() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda0
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                MonthlyReportFragment.this.m238x10b5333e((List) obj);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$loadData$7$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m238x10b5333e(final List students) {
        this.currentStudents = students;
        String fromDate = String.format(Locale.US, "%04d-%02d-01", Integer.valueOf(this.selectedYear), Integer.valueOf(this.selectedMonth + 1));
        String toDate = String.format(Locale.US, "%04d-%02d-31", Integer.valueOf(this.selectedYear), Integer.valueOf(this.selectedMonth + 1));
        this.viewModel.getAttendanceInRange(this.className, fromDate, toDate).observe(getViewLifecycleOwner(), new Observer() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda12
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                MonthlyReportFragment.this.m237xcd2a157d(students, (List) obj);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$loadData$6$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m237xcd2a157d(List students, List attendance) {
        this.currentAttendance = attendance;
        renderTable(students, attendance);
    }

    private void renderTable(List<StudentEntity> students, List<AttendanceEntity> attendance) {
        this.headerContainer.removeAllViews();
        this.rollNoContainer.removeAllViews();
        this.contentContainer.removeAllViews();
        if (students == null || students.isEmpty()) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(this.selectedYear, this.selectedMonth, 1);
        int daysInMonth = cal.getActualMaximum(5);
        int maxNameWidth = ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION;
        Paint textPaint = new Paint();
        char c = 2;
        float textSizePixels = TypedValue.applyDimension(2, 14.0f, getResources().getDisplayMetrics());
        textPaint.setTextSize(textSizePixels);
        Iterator<StudentEntity> it = students.iterator();
        while (it.hasNext()) {
            float width = textPaint.measureText(it.next().name) + 80.0f;
            if (width > maxNameWidth) {
                maxNameWidth = (int) width;
            }
        }
        this.headerContainer.addView(createCell("Name", maxNameWidth, true));
        for (int day = 1; day <= daysInMonth; day++) {
            this.headerContainer.addView(createCell(String.valueOf(day), 80, true));
        }
        Map<String, Map<Integer, String>> attendanceMap = new HashMap<>();
        Iterator<AttendanceEntity> it2 = attendance.iterator();
        while (true) {
            String str = "P";
            if (!it2.hasNext()) {
                break;
            }
            AttendanceEntity att = it2.next();
            int day2 = Integer.parseInt(att.date.split("-")[c]);
            Map<Integer, String> mapComputeIfAbsent = attendanceMap.computeIfAbsent(att.rollNo, new Function() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda3
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return MonthlyReportFragment.lambda$renderTable$8((String) obj);
                }
            });
            Integer numValueOf = Integer.valueOf(day2);
            if (!att.isPresent) {
                str = "A";
            }
            mapComputeIfAbsent.put(numValueOf, str);
            c = 2;
        }
        for (StudentEntity student : students) {
            TextView rollView = createCell(student.rollNo, -1, false);
            rollView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
            this.rollNoContainer.addView(rollView);
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(0);
            TextView nameCell = createCell(student.name, maxNameWidth, false);
            nameCell.setGravity(NavigationBarView.ITEM_GRAVITY_START_CENTER);
            nameCell.setPadding(30, 0, 30, 0);
            rowLayout.addView(nameCell);
            int day3 = 1;
            while (day3 <= daysInMonth) {
                Calendar cal2 = cal;
                int daysInMonth2 = daysInMonth;
                String status = attendanceMap.getOrDefault(student.rollNo, new HashMap<>()).getOrDefault(Integer.valueOf(day3), "");
                int maxNameWidth2 = maxNameWidth;
                TextView cell = createCell(status, 80, false);
                if (status.equals("P")) {
                    cell.setTextColor(Color.parseColor("#4CAF50"));
                    cell.setTypeface(null, 1);
                } else if (status.equals("A")) {
                    cell.setTextColor(Color.parseColor("#F44336"));
                    cell.setTypeface(null, 1);
                }
                rowLayout.addView(cell);
                day3++;
                maxNameWidth = maxNameWidth2;
                cal = cal2;
                daysInMonth = daysInMonth2;
            }
            this.contentContainer.addView(rowLayout);
            cal = cal;
        }
    }

    static /* synthetic */ Map lambda$renderTable$8(String k) {
        return new HashMap();
    }

    private TextView createCell(String text, int width, boolean isHeader) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        int cellWidth = width != -1 ? width : -1;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cellWidth, 80);
        tv.setLayoutParams(params);
        tv.setSingleLine(true);
        tv.setHorizontallyScrolling(true);
        tv.setGravity(17);
        tv.setBackgroundResource(R.drawable.bg_table_cell);
        if (isHeader) {
            TypedValue typedValue = new TypedValue();
            Context context = getContext();
            if (context != null) {
                // Background color from theme
                context.getTheme().resolveAttribute(R.attr.appBackgroundColor, typedValue, true);
                tv.setBackgroundColor(typedValue.data);

                // Text color from theme
                context.getTheme().resolveAttribute(R.attr.appPrimaryColor, typedValue, true);
                tv.setTextColor(typedValue.data);
            }
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setTextColor(Color.BLACK);
        }
        return tv;
    }

    private void showMonthYearPicker() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        materialAlertDialogBuilder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded));
        materialAlertDialogBuilder.setTitle((CharSequence) "Select Month & Year");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(0);
        layout.setGravity(17);
        layout.setPadding(30, 30, 30, 30);
        final NumberPicker monthPicker = new NumberPicker(getContext());
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        monthPicker.setValue(this.selectedMonth);
        final NumberPicker yearPicker = new NumberPicker(getContext());
        yearPicker.setMinValue(2020);
        yearPicker.setMaxValue(2030);
        yearPicker.setValue(this.selectedYear);
        layout.addView(monthPicker);
        layout.addView(yearPicker);
        materialAlertDialogBuilder.setView((View) layout);
        materialAlertDialogBuilder.setPositiveButton((CharSequence) "OK", new DialogInterface.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda4
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MonthlyReportFragment.this.m245xc5b9a384(monthPicker, yearPicker, dialogInterface, i);
            }
        });
        materialAlertDialogBuilder.setNegativeButton((CharSequence) "Cancel", (DialogInterface.OnClickListener) null);
        materialAlertDialogBuilder.show();
    }

    /* JADX INFO: renamed from: lambda$showMonthYearPicker$9$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m245xc5b9a384(NumberPicker monthPicker, NumberPicker yearPicker, DialogInterface dialog, int which) {
        this.selectedMonth = monthPicker.getValue();
        this.selectedYear = yearPicker.getValue();
        updateMonthLabel();
        loadData();
    }

    private void updateMonthLabel() {
        this.btnMonthPicker.setText(String.format(Locale.US, "%tB %d", new GregorianCalendar(this.selectedYear, this.selectedMonth, 1), Integer.valueOf(this.selectedYear)));
    }

    private void showSaveDialog(String defaultName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle((CharSequence) "Export Monthly");
        builder.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_export_summary, (ViewGroup) null);
        final TextInputEditText input = (TextInputEditText) view.findViewById(R.id.etFilename);
        final RadioButton rbExcel = (RadioButton) view.findViewById(R.id.rbExcel);
        input.setText(defaultName);
        builder.setView(view);
        builder.setPositiveButton((CharSequence) "SAVE", (DialogInterface.OnClickListener) null);
        builder.setNegativeButton((CharSequence) "CANCEL", new DialogInterface.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(-1).setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                MonthlyReportFragment.this.m246x611dbf23(input, rbExcel, dialog, view2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$showSaveDialog$11$com-example-attnd-fragments-MonthlyReportFragment, reason: not valid java name */
    /* synthetic */ void m246x611dbf23(TextInputEditText input, RadioButton rbExcel, AlertDialog dialog, View v) {
        String fileName = input.getText().toString().trim();
        if (fileName.isEmpty()) {
            Toast.makeText(getContext(), "Filename cannot be empty", 0).show();
            return;
        }
        boolean isExcel = rbExcel.isChecked();
        String extension = isExcel ? ".xls" : ".pdf";
        if (!fileName.endsWith(extension)) {
            fileName = fileName + extension;
        }
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ATTND");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);
        if (file.exists()) {
            Toast.makeText(getContext(), "File already exists!", 0).show();
            return;
        }
        if (isExcel) {
            exportToExcel(file);
        } else {
            exportToPdf(file);
        }
        dialog.dismiss();
    }

    private void exportToExcel(File file) {
        HSSFWorkbook workbook;
        HSSFSheet sheet;
        int daysInMonth;
        Map<String, Map<Integer, String>> attendanceMap;
        Iterator<AttendanceEntity> it;
        int i = 0;
        try {
            workbook = new HSSFWorkbook();
            sheet = workbook.createSheet("Attendance Report");
            Calendar cal = Calendar.getInstance();
            cal.set(this.selectedYear, this.selectedMonth, 1);
            daysInMonth = cal.getActualMaximum(5);
            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Roll No");
            headerRow.createCell(1).setCellValue("Name");
            for (int d = 1; d <= daysInMonth; d++) {
                headerRow.createCell(d + 1).setCellValue(d);
            }
            int d2 = daysInMonth + 2;
            headerRow.createCell(d2).setCellValue("Total P");
            headerRow.createCell(daysInMonth + 3).setCellValue("Total A");
            attendanceMap = new HashMap<>();
            it = this.currentAttendance.iterator();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error exporting Excel: " + e.getMessage(), 0).show();
            return;
        }
        while (true) {
            String str = "P";
            if (!it.hasNext()) {
                break;
            }
            AttendanceEntity att = it.next();
            int day = Integer.parseInt(att.date.split("-")[2]);
            Map<Integer, String> mapComputeIfAbsent = attendanceMap.computeIfAbsent(att.rollNo, new Function() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda11
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return MonthlyReportFragment.lambda$exportToExcel$12((String) obj);
                }
            });
            Integer numValueOf = Integer.valueOf(day);
            if (!att.isPresent) {
                str = "A";
            }
            mapComputeIfAbsent.put(numValueOf, str);
        }
        int rowIndex = 1;
        for (StudentEntity s : this.currentStudents) {
            int rowIndex2 = rowIndex + 1;
            HSSFRow row = sheet.createRow(rowIndex);
            row.createCell(i).setCellValue(s.rollNo);
            row.createCell(1).setCellValue(s.name);
            int pCount = 0;
            int aCount = 0;
            int d3 = 1;
            while (d3 <= daysInMonth) {
                int d4 = d3;
                HSSFSheet sheet2 = sheet;
                String status = attendanceMap.getOrDefault(s.rollNo, new HashMap<>()).getOrDefault(Integer.valueOf(d4), "");
                row.createCell(d4 + 1).setCellValue(status);
                if (status.equals("P")) {
                    pCount++;
                }
                if (status.equals("A")) {
                    aCount++;
                }
                d3 = d4 + 1;
                sheet = sheet2;
            }
            row.createCell(daysInMonth + 2).setCellValue(pCount);
            row.createCell(daysInMonth + 3).setCellValue(aCount);
            i = 0;
            rowIndex = rowIndex2;
            sheet = sheet;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            Toast.makeText(getContext(), "Saved Excel: " + file.getName(), 1).show();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    static /* synthetic */ Map lambda$exportToExcel$12(String k) {
        return new HashMap();
    }

    private void exportToPdf(File file) {
        Paint paint;
        float startX;
        char c;
        int daysInMonth;
        float rollWidth;
        int pageWidth;
        int pageHeight;
        float nameStartX;
        float dateStartX;
        float cellWidth;
        float rowHeight;
        Map<String, Map<Integer, String>> attendanceMap;
        int pageHeight2;
        float nameStartX2;
        Canvas canvas;
        PdfDocument.Page page;
        float y;
        float cellWidth2;
        PdfDocument document = new PdfDocument();
        Paint paint2 = new Paint();
        paint2.setTextSize(8.0f);
        float maxNameWidth = paint2.measureText("Name") + 10.0f;
        Iterator<StudentEntity> it = this.currentStudents.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            StudentEntity s = it.next();
            String displayName = s.name != null ? s.name : "";
            if (displayName.length() > 35) {
                displayName = displayName.substring(0, 32) + "...";
            }
            float width = paint2.measureText(displayName);
            if (width > maxNameWidth) {
                maxNameWidth = width;
            }
        }
        float maxNameWidth2 = maxNameWidth + 15.0f;
        Calendar cal = Calendar.getInstance();
        cal.set(this.selectedYear, this.selectedMonth, 1);
        int daysInMonth2 = cal.getActualMaximum(5);
        float totalTableWidth = 30.0f + maxNameWidth2 + (daysInMonth2 * 18.0f);
        float startX2 = (842.0f - totalTableWidth) / 2.0f;
        if (startX2 < 10.0f) {
            startX2 = 10.0f;
        }
        float startX3 = startX2;
        float nameStartX3 = startX3 + 30.0f;
        float dateStartX2 = nameStartX3 + maxNameWidth2;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page2 = document.startPage(pageInfo);
        Canvas canvas2 = page2.getCanvas();
        canvas2.translate(595, 0.0f);
        canvas2.rotate(90.0f);
        try {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.attnd);
            if (drawable == null) {
                paint = paint2;
                startX = startX3;
                c = 0;
                daysInMonth = daysInMonth2;
                rollWidth = 30.0f;
                pageWidth = 595;
                pageHeight = 842;
            } else {
                paint2.setTextSize(16.0f);
                paint2.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
                float brandTextWidth = paint2.measureText("ATTND");
                paint2.setTextSize(8.0f);
                try {
                    paint2.setTypeface(Typeface.create(Typeface.DEFAULT, 0));
                    float subTextWidth = paint2.measureText("By Nirbhay D. & Parth K.");
                    float maxTextWidth = Math.max(brandTextWidth, subTextWidth);
                    rollWidth = 30.0f;
                    float rollWidth2 = (842.0f - maxTextWidth) - 20.0f;
                    float effectivePageWidth = (842.0f - maxTextWidth) - 20.0f;
                    pageWidth = 595;
                    pageHeight = 842;
                    float subTextY = 36.0f + 12.0f;
                    daysInMonth = daysInMonth2;
                    try {
                        paint2.setTextSize(16.0f);
                        paint2.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
                        paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
                        canvas2.drawText("ATTND", rollWidth2, 36.0f, paint2);
                        try {
                            paint2.setTextSize(8.0f);
                            paint2.setTypeface(Typeface.create(Typeface.DEFAULT, 0));
                            canvas2.drawText("By Nirbhay D. & Parth K.", effectivePageWidth, subTextY, paint2);
                            float lineX = (842.0f - maxTextWidth) - 30.0f;
                            paint2.setColor(Color.argb(80, 0, 0, 0));
                            paint2.setStrokeWidth(1.5f);
                            float subTextWidth2 = 32.0f + 15.0f;
                            paint = paint2;
                            startX = startX3;
                            c = 0;
                            try {
                                canvas2.drawLine(lineX, 15.0f, lineX, subTextWidth2, paint);
                                float logoX = (lineX - 10.0f) - 32.0f;
                                drawable.setBounds((int) logoX, 15, (int) (logoX + 32.0f), (int) (32.0f + 15.0f));
                                drawable.draw(canvas2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            paint = paint2;
                            startX = startX3;
                            c = 0;
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        paint = paint2;
                        startX = startX3;
                        c = 0;
                    }
                } catch (Exception e4) {
                    e4.printStackTrace();
                    paint = paint2;
                    startX = startX3;
                    daysInMonth = daysInMonth2;
                    rollWidth = 30.0f;
                    pageWidth = 595;
                    pageHeight = 842;
                    c = 0;
                }
            }
        } catch (Exception e5) {
            e5.printStackTrace();
            paint = paint2;
            startX = startX3;
            c = 0;
            daysInMonth = daysInMonth2;
            rollWidth = 30.0f;
            pageWidth = 595;
            pageHeight = 842;
        }
        paint.setTextSize(14.0f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        canvas2.drawText(" Class Name: " + (this.className != null ? this.className : ""), startX, 25.0f, paint);
        float y2 = 25.0f + 21.0f;
        canvas2.drawText(" " + this.btnMonthPicker.getText().toString(), startX, y2, paint);
        float y3 = y2 + 22.0f;
        Map<String, Map<Integer, String>> attendanceMap2 = new HashMap<>();
        Iterator<AttendanceEntity> it2 = this.currentAttendance.iterator();
        while (true) {
            String str = "P";
            if (!it2.hasNext()) {
                break;
            }
            AttendanceEntity att = it2.next();
            int day = Integer.parseInt(att.date.split("-")[2]);
            Map<Integer, String> mapComputeIfAbsent = attendanceMap2.computeIfAbsent(att.rollNo, new Function() { // from class: com.example.attnd.fragments.MonthlyReportFragment$$ExternalSyntheticLambda13
                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return MonthlyReportFragment.lambda$exportToPdf$13((String) obj);
                }
            });
            Integer numValueOf = Integer.valueOf(day);
            if (!att.isPresent) {
                str = "A";
            }
            mapComputeIfAbsent.put(numValueOf, str);
        }
        String attendanceMap3 = "A";
        Object obj = "P";
        float dateStartX3 = 18.0f;
        float startX4 = nameStartX3;
        float nameStartX4 = dateStartX2;
        int daysInMonth3 = daysInMonth;
        float rollWidth3 = rollWidth;
        float startX5 = startX;
        Paint paint3 = paint;
        float rowHeight2 = 16.0f;
        float y4 = drawTableHeader(canvas2, paint3, startX5, y3, startX4, nameStartX4, 18.0f, 16.0f, daysInMonth3, rollWidth3);
        Canvas canvas3 = canvas2;
        float startX6 = startX5;
        String str2 = " ";
        paint3.setTypeface(Typeface.create(Typeface.DEFAULT, 0));
        Iterator<StudentEntity> it3 = this.currentStudents.iterator();
        PdfDocument.Page page3 = page2;
        while (it3.hasNext()) {
            Iterator<StudentEntity> it4 = it3;
            StudentEntity s2 = it3.next();
            if (y4 <= 595.0f - 30.0f) {
                nameStartX = startX4;
                dateStartX = nameStartX4;
                cellWidth = dateStartX3;
                rowHeight = rowHeight2;
                attendanceMap = attendanceMap2;
                pageHeight2 = pageHeight;
                nameStartX2 = startX6;
                canvas = canvas3;
                page = page3;
                y = y4;
            } else {
                document.finishPage(page3);
                float startX7 = startX6;
                attendanceMap = attendanceMap2;
                int pageWidth2 = pageWidth;
                pageHeight2 = pageHeight;
                PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(pageWidth2, pageHeight2, document.getPages().size() + 1).create();
                PdfDocument.Page page4 = document.startPage(pageInfo2);
                canvas = page4.getCanvas();
                canvas.translate(pageWidth2, 0.0f);
                canvas.rotate(90.0f);
                float y5 = drawTableHeader(canvas, paint3, startX7, 25.0f, startX4, nameStartX4, dateStartX3, rowHeight2, daysInMonth3, rollWidth3);
                nameStartX = startX4;
                dateStartX = nameStartX4;
                cellWidth = dateStartX3;
                rowHeight = rowHeight2;
                nameStartX2 = startX7;
                paint3.setTypeface(Typeface.create(Typeface.DEFAULT, 0));
                page = page4;
                y = y5;
            }
            paint3.setTextSize(8.0f);
            paint3.setColor(ViewCompat.MEASURED_STATE_MASK);
            float textY = y + 11.0f;
            float rollTextWidth = paint3.measureText(s2.rollNo);
            float rollXOffset = (rollWidth3 - rollTextWidth) / 2.0f;
            canvas.drawText(s2.rollNo, nameStartX2 + rollXOffset, textY, paint3);
            String displayName2 = s2.name != null ? s2.name : "";
            canvas.drawText(displayName2.length() > 35 ? displayName2.substring(0, 32) + "...." : displayName2, nameStartX + 3.0f, textY, paint3);
            int d = 1;
            while (d <= daysInMonth3) {
                Map<String, Map<Integer, String>> attendanceMap4 = attendanceMap;
                float rollWidth4 = rollWidth3;
                String str3 = str2;
                String status = attendanceMap4.getOrDefault(s2.rollNo, new HashMap<>()).getOrDefault(Integer.valueOf(d), str3);
                Object obj2 = obj;
                if (status.equals(obj2)) {
                    cellWidth2 = cellWidth;
                    paint3.setColor(Color.parseColor("#006400"));
                } else {
                    cellWidth2 = cellWidth;
                    if (status.equals(attendanceMap3)) {
                        paint3.setColor(SupportMenu.CATEGORY_MASK);
                    } else {
                        paint3.setColor(-7829368);
                    }
                }
                float textWidth = paint3.measureText(status);
                float xOffset = (cellWidth2 - textWidth) / 2.0f;
                canvas.drawText(status, dateStartX + ((d - 1) * cellWidth2) + xOffset, textY, paint3);
                d++;
                obj = obj2;
                cellWidth = cellWidth2;
                attendanceMap3 = attendanceMap3;
                str2 = str3;
                attendanceMap = attendanceMap4;
                rollWidth3 = rollWidth4;
            }
            float rollWidth5 = rollWidth3;
            String str4 = str2;
            float cellWidth3 = cellWidth;
            paint3.setColor(ViewCompat.MEASURED_STATE_MASK);
            paint3.setStrokeWidth(0.5f);
            float textY2 = nameStartX2;
            canvas.drawLine(textY2, y + rowHeight, dateStartX + (daysInMonth3 * cellWidth3), y + rowHeight, paint3);
            float y6 = y;
            float nameStartX5 = nameStartX;
            drawVerticalLines(canvas, paint3, textY2, y6, y + rowHeight, nameStartX5, dateStartX, cellWidth3, daysInMonth3);
            dateStartX3 = cellWidth3;
            y4 = y6 + rowHeight;
            canvas3 = canvas;
            startX6 = textY2;
            startX4 = nameStartX5;
            str2 = str4;
            pageHeight = pageHeight2;
            obj = obj;
            it3 = it4;
            attendanceMap3 = attendanceMap3;
            rowHeight2 = rowHeight;
            nameStartX4 = dateStartX;
            rollWidth3 = rollWidth5;
            attendanceMap2 = attendanceMap;
            page3 = page;
        }
        document.finishPage(page3);
        try {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                document.writeTo(fos);
                document.close();
                fos.close();
                Toast.makeText(getContext(), "Saved PDF: " + file.getName(), 1).show();
            } catch (Exception e6) {
                Toast.makeText(getContext(), "Error exporting PDF: " + e6.getMessage(), 0).show();
            }
        } catch (Exception e7) {
            e7.printStackTrace();
        }
    }

    static /* synthetic */ Map lambda$exportToPdf$13(String k) {
        return new HashMap();
    }

    private float drawTableHeader(Canvas canvas, Paint paint, float startX, float y, float nameStartX, float dateStartX, float cellWidth, float rowHeight, int daysInMonth, float rollWidth) {
        paint.setTextSize(8.0f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        paint.setStrokeWidth(0.5f);
        canvas.drawLine(startX, y, dateStartX + (daysInMonth * cellWidth), y, paint);
        float textY = y + 11.0f;
        float rollTextWidth = paint.measureText("Roll");
        float rollXOffset = (rollWidth - rollTextWidth) / 2.0f;
        canvas.drawText("Roll", startX + rollXOffset, textY, paint);
        canvas.drawText("Name", nameStartX + 3.0f, textY, paint);
        for (int d = 1; d <= daysInMonth; d++) {
            float textWidth = paint.measureText(String.valueOf(d));
            float xOffset = (cellWidth - textWidth) / 2.0f;
            canvas.drawText(String.valueOf(d), dateStartX + ((d - 1) * cellWidth) + xOffset, textY, paint);
        }
        canvas.drawLine(startX, y + rowHeight, dateStartX + (daysInMonth * cellWidth), y + rowHeight, paint);
        drawVerticalLines(canvas, paint, startX, y, y + rowHeight, nameStartX, dateStartX, cellWidth, daysInMonth);
        return y + rowHeight;
    }

    private void drawVerticalLines(Canvas canvas, Paint paint, float startX, float topY, float bottomY, float nameStartX, float dateStartX, float cellWidth, int daysInMonth) {
        paint.setStrokeWidth(0.5f);
        canvas.drawLine(startX, topY, startX, bottomY, paint);
        canvas.drawLine(nameStartX, topY, nameStartX, bottomY, paint);
        canvas.drawLine(dateStartX, topY, dateStartX, bottomY, paint);
        for (int d = 1; d <= daysInMonth; d++) {
            float x = dateStartX + (d * cellWidth);
            canvas.drawLine(x, topY, x, bottomY, paint);
        }
    }
}

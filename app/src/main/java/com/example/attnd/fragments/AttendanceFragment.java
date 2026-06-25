package com.example.attnd.fragments;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;
import com.example.attnd.ClassWidgetProvider;
import com.example.attnd.R;
import com.example.attnd.adapter.AttendanceAdapter;
import com.example.attnd.database.AttendanceEntity;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.database.StudentEntity;
import com.example.attnd.viewmodel.MainViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceFragment extends Fragment {
    private AttendanceAdapter adapter;
    private Button btnNavigateUpdate;
    private Button btnSave;
    private LiveData<List<AttendanceEntity>> currentAttendanceLiveData;
    private LiveData<List<StudentEntity>> currentStudentsLiveData;
    private View headerRow;
    private View layoutEmptyState;
    private ListPopupWindow listPopupWindow;
    private RecyclerView rvAttendance;
    private String selectedClass;
    private String selectedDate;
    private SwitchCompat toggleAttendance;
    private TextView tvCenterMessage;
    private TextView tvClassSelector;
    private TextView tvDate;
    private MainViewModel viewModel;
    private List<ClassEntity> classList = new ArrayList<>();
    private List<StudentEntity> currentStudents = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        this.tvClassSelector = view.findViewById(R.id.tvClassSelector);
        this.tvDate = view.findViewById(R.id.tvDate);
        this.toggleAttendance = view.findViewById(R.id.toggleAttendance);
        this.btnSave = view.findViewById(R.id.btnSave);
        this.rvAttendance = view.findViewById(R.id.rvAttendance);
        this.layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        this.tvCenterMessage = view.findViewById(R.id.tvCenterMessage);
        this.btnNavigateUpdate = view.findViewById(R.id.btnNavigateUpdate);
        this.headerRow = view.findViewById(R.id.headerRow);

        this.rvAttendance.setLayoutManager(new LinearLayoutManager(getContext()));
        this.adapter = new AttendanceAdapter();
        this.rvAttendance.setAdapter(this.adapter);

        showEmptyState("Please select a class", false);

        if (this.viewModel.savedSelectedClass != null) {
            this.selectedClass = this.viewModel.savedSelectedClass;
            this.tvClassSelector.setText(this.selectedClass);
            this.adapter.setAttendanceData(this.viewModel.savedAttendanceStatus);
        }

        Calendar cal = Calendar.getInstance();
        updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        this.tvDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> updateDate(year, month, day),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        this.toggleAttendance.setOnCheckedChangeListener((buttonView, isChecked) -> this.adapter.setAllStatus(isChecked));

        this.listPopupWindow = new ListPopupWindow(requireContext());
        this.listPopupWindow.setAnchorView(this.tvClassSelector);
        this.listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_popup_rounded));

        this.viewModel.getAllClasses().observe(getViewLifecycleOwner(), classes -> {
            this.classList = classes;
            List<String> names = new ArrayList<>();
            for (ClassEntity c : classes) {
                names.add(c.className);
            }
            if (names.size() > 5) {
                this.listPopupWindow.setHeight(500);
            } else {
                this.listPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            this.listPopupWindow.setWidth(ListPopupWindow.WRAP_CONTENT);
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, names);
            this.listPopupWindow.setAdapter(spinAdapter);
            this.listPopupWindow.setOnItemClickListener((parent, view12, position, id) -> {
                this.selectedClass = this.classList.get(position).className;
                this.tvClassSelector.setText(this.selectedClass);
                this.viewModel.savedSelectedClass = this.selectedClass;
                this.viewModel.savedAttendanceStatus = new HashMap<>();
                this.adapter.clearData();
                this.listPopupWindow.dismiss();
                checkAttendanceAndLoad();
            });
        });

        this.tvClassSelector.setOnClickListener(v -> {
            this.listPopupWindow.setVerticalOffset(16);
            this.listPopupWindow.show();
        });

        this.btnSave.setOnClickListener(v -> {
            if (this.selectedClass == null) {
                Toast.makeText(getContext(), "Please select a class", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> data = this.adapter.getAttendanceData();
            List<AttendanceEntity> records = new ArrayList<>();
            for (StudentEntity student : this.currentStudents) {
                String status = data.get(student.rollNo);
                if (status != null) {
                    AttendanceEntity entity = new AttendanceEntity(this.selectedDate, student.rollNo, this.selectedClass, status, this.viewModel.getTargetTeacherId());
                    entity.studentName = student.name;
                    records.add(entity);
                }
            }
            this.viewModel.saveAttendance(records);
            Toast.makeText(getContext(), "Saved for " + this.selectedDate, Toast.LENGTH_SHORT).show();
            
            Intent widgetIntent = new Intent(getContext(), ClassWidgetProvider.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(getContext()).getAppWidgetIds(new ComponentName(requireContext(), ClassWidgetProvider.class));
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            requireContext().sendBroadcast(widgetIntent);
            
            checkAttendanceAndLoad();
        });

        this.btnNavigateUpdate.setOnClickListener(v -> {
            if (this.selectedClass == null) {
                Toast.makeText(getContext(), "Please select a class", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putString("className", this.selectedClass);
            Navigation.findNavController(v).navigate(R.id.nav_daily_report, args);
        });

        if (getArguments() != null && getArguments().containsKey("className")) {
            String incomingClass = getArguments().getString("className");
            if (incomingClass != null) {
                this.selectedClass = incomingClass;
                this.tvClassSelector.setText(this.selectedClass);
                checkAttendanceAndLoad();
            }
        }
        return view;
    }

    private void updateDate(int year, int month, int day) {
        this.selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
        this.tvDate.setText(this.selectedDate);
        if (this.selectedClass != null) {
            checkAttendanceAndLoad();
        }
    }

    private void checkAttendanceAndLoad() {
        if (this.selectedClass == null) {
            showEmptyState("Please select a class", false);
            return;
        }
        if (this.currentAttendanceLiveData != null) {
            this.currentAttendanceLiveData.removeObservers(getViewLifecycleOwner());
        }
        this.currentAttendanceLiveData = this.viewModel.getDailyAttendance(this.selectedClass, this.selectedDate);
        this.currentAttendanceLiveData.observe(getViewLifecycleOwner(), attendanceList -> {
            if (attendanceList != null && !attendanceList.isEmpty()) {
                showEmptyState("Today's attendance has been recorded.", true);
            } else {
                hideEmptyState();
                loadStudentsOnly();
            }
        });
    }

    private void loadStudentsOnly() {
        if (this.currentStudentsLiveData != null) {
            this.currentStudentsLiveData.removeObservers(getViewLifecycleOwner());
        }
        this.currentStudentsLiveData = this.viewModel.getStudents(this.selectedClass);
        this.currentStudentsLiveData.observe(getViewLifecycleOwner(), students -> {
            this.currentStudents = students;
            this.adapter.setStudents(students);
        });
    }

    private void showEmptyState(String message, boolean showUpdateBtn) {
        if (this.layoutEmptyState != null) this.layoutEmptyState.setVisibility(View.VISIBLE);
        if (this.tvCenterMessage != null) this.tvCenterMessage.setText(message);
        if (this.btnNavigateUpdate != null) this.btnNavigateUpdate.setVisibility(showUpdateBtn ? View.VISIBLE : View.GONE);
        if (this.rvAttendance != null) this.rvAttendance.setVisibility(View.GONE);
        if (this.headerRow != null) this.headerRow.setVisibility(View.GONE);
        if (this.btnSave != null) this.btnSave.setVisibility(View.GONE);
        if (this.toggleAttendance != null) this.toggleAttendance.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        if (this.layoutEmptyState != null) this.layoutEmptyState.setVisibility(View.GONE);
        if (this.rvAttendance != null) this.rvAttendance.setVisibility(View.VISIBLE);
        if (this.headerRow != null) this.headerRow.setVisibility(View.VISIBLE);
        if (this.btnSave != null) this.btnSave.setVisibility(View.VISIBLE);
        if (this.toggleAttendance != null) this.toggleAttendance.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.adapter != null && this.selectedClass != null) {
            this.viewModel.savedAttendanceStatus = this.adapter.getAttendanceData();
        }
    }
}

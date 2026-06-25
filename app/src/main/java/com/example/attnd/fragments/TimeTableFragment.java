package com.example.attnd.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.attnd.R;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.database.TimeTableEntity;
import com.example.attnd.utils.AlarmHelper;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.common.net.HttpHeaders;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes9.dex */
public class TimeTableFragment extends Fragment {
    private LinearLayout contentContainer;
    private TextView tvCurrentDay;
    private MainViewModel viewModel;
    private List<String> classNamesList = new ArrayList();
    private List<TimeTableEntity> currentRows = new ArrayList();
    private String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String[] dbDayKeys = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private int currentDayIndex = 0;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);
        this.viewModel = (MainViewModel) new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.contentContainer = (LinearLayout) view.findViewById(R.id.contentContainer);
        this.tvCurrentDay = (TextView) view.findViewById(R.id.tvCurrentDay);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(7);
        if (dayOfWeek == 1) {
            this.currentDayIndex = 0;
        } else {
            this.currentDayIndex = dayOfWeek - 2;
        }
        ImageButton backButton = (ImageButton) view.findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    TimeTableFragment.this.m304x16ae0827(view2);
                }
            });
        }
        view.findViewById(R.id.btnPrevDay).setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                TimeTableFragment.this.m305x8c282e68(view2);
            }
        });
        view.findViewById(R.id.btnNextDay).setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                TimeTableFragment.this.m306x1a254a9(view2);
            }
        });
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(requireContext(), "android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
        }
        this.viewModel.getAllClasses().observe(getViewLifecycleOwner(), new Observer() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda9
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                TimeTableFragment.this.m307x771c7aea((List) obj);
            }
        });
        this.viewModel.getTimeTable().observe(getViewLifecycleOwner(), new Observer() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda10
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                TimeTableFragment.this.m308xec96a12b((List) obj);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabAddRow);
        fab.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                TimeTableFragment.this.m309x6210c76c(view2);
            }
        });
        updateDayView();
        return view;
    }

    /* JADX INFO: renamed from: lambda$onCreateView$0$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m304x16ae0827(View v) {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    /* JADX INFO: renamed from: lambda$onCreateView$1$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m305x8c282e68(View v) {
        this.currentDayIndex = this.currentDayIndex == 0 ? 5 : this.currentDayIndex - 1;
        changeDayWithAnimation(false);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$2$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m306x1a254a9(View v) {
        this.currentDayIndex = this.currentDayIndex == 5 ? 0 : this.currentDayIndex + 1;
        changeDayWithAnimation(true);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$3$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m307x771c7aea(List classes) {
        this.classNamesList.clear();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            ClassEntity c = (ClassEntity) it.next();
            this.classNamesList.add(c.className);
        }
        this.classNamesList.add("-");
    }

    /* JADX INFO: renamed from: lambda$onCreateView$4$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m308xec96a12b(List rows) {
        this.currentRows = rows;
        renderTable(rows);
        AlarmHelper.scheduleTimetableAlarms(requireContext(), rows);
    }

    /* JADX INFO: renamed from: lambda$onCreateView$5$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m309x6210c76c(View v) {
        TimeTableEntity newRow = new TimeTableEntity(this.viewModel.getTargetTeacherId());
        this.viewModel.addTimeTableRow(newRow);
    }

    private void updateDayView() {
        this.tvCurrentDay.setText(this.daysOfWeek[this.currentDayIndex]);
        renderTable(this.currentRows);
    }

    private void changeDayWithAnimation(boolean isNext) {
        float exitTranslationX = isNext ? -50.0f : 50.0f;
        final float startTranslationX = isNext ? 50.0f : -50.0f;
        this.tvCurrentDay.animate().alpha(0.0f).translationX(exitTranslationX).setDuration(150L).withEndAction(new Runnable() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                TimeTableFragment.this.m303x8b8e535f(startTranslationX);
            }
        }).start();
        renderTable(this.currentRows);
    }

    /* JADX INFO: renamed from: lambda$changeDayWithAnimation$6$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m303x8b8e535f(float startTranslationX) {
        this.tvCurrentDay.setText(this.daysOfWeek[this.currentDayIndex]);
        this.tvCurrentDay.setTranslationX(startTranslationX);
        this.tvCurrentDay.animate().alpha(1.0f).translationX(0.0f).setDuration(150L).start();
    }

    private void renderTable(List<TimeTableEntity> rows) {
        this.contentContainer.removeAllViews();
        if (rows == null) {
            return;
        }

        TypedValue typedValue = new TypedValue();
        int primaryColor = Color.parseColor("#0c56cd");
        int backgroundColor = Color.TRANSPARENT; 
        int dividerColor = Color.parseColor("#EAEAEA");

        Context context = getContext();
        if (context != null) {
            Resources.Theme theme = context.getTheme();
            if (theme.resolveAttribute(R.attr.appPrimaryColor, typedValue, true)) {
                primaryColor = typedValue.data;
            }
            if (theme.resolveAttribute(R.attr.appBackgroundColor, typedValue, true)) {
                backgroundColor = -1;
            }
            if (theme.resolveAttribute(R.attr.appDividerColor, typedValue, true)) {
                dividerColor = typedValue.data;
            }
        }

        for (final TimeTableEntity row : rows) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(0);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            linearLayout.setMinimumHeight(dpToPx(65));
            linearLayout.setBackgroundColor(backgroundColor);
            linearLayout.setGravity(16);
            TextView timeCell = new TextView(getContext());
            String timeText = formatTo12Hour(row.startTime) + "\n-\n" + formatTo12Hour(row.endTime);
            timeCell.setText(timeText);
            timeCell.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(120), -2));
            timeCell.setGravity(17);
            timeCell.setTextColor(primaryColor);
            timeCell.setTypeface(null, 1);
            timeCell.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            timeCell.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    TimeTableFragment.this.m313xa7a217ac(row, view);
                }
            });
            timeCell.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda2
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return TimeTableFragment.this.m314x1d1c3ded(row, view);
                }
            });
            View verticalDivider = new View(getContext());
            verticalDivider.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(1), -1));
            verticalDivider.setBackgroundColor(dividerColor);
            LinearLayout classLayout = new LinearLayout(getContext());
            classLayout.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            classLayout.setOrientation(0);
            classLayout.setGravity(16);
            classLayout.setPadding(dpToPx(24), dpToPx(8), dpToPx(24), dpToPx(8));
            classLayout.setOnClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    TimeTableFragment.this.m315x9296642e(row, view);
                }
            });
            TextView classCell = new TextView(getContext());
            String currentClass = getCurrentDayClass(row);
            classCell.setText(currentClass);
            classCell.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            classCell.setGravity(17);
            classCell.setTextColor(ViewCompat.MEASURED_STATE_MASK);
            classCell.setTextSize(15.0f);
            classLayout.addView(classCell);
            linearLayout.addView(timeCell);
            linearLayout.addView(verticalDivider);
            linearLayout.addView(classLayout);
            View rowDivider = new View(getContext());
            rowDivider.setLayoutParams(new LinearLayout.LayoutParams(-1, dpToPx(1)));
            rowDivider.setBackgroundColor(dividerColor);
            this.contentContainer.addView(linearLayout);
            this.contentContainer.addView(rowDivider);
        }
    }

    /* JADX INFO: renamed from: lambda$renderTable$7$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m313xa7a217ac(TimeTableEntity row, View v) {
        onTimeCellClicked(row);
    }

    /* JADX INFO: renamed from: lambda$renderTable$8$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ boolean m314x1d1c3ded(TimeTableEntity row, View v) {
        showDeleteDialog(row);
        return true;
    }

    /* JADX INFO: renamed from: lambda$renderTable$9$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m315x9296642e(TimeTableEntity row, View v) {
        onDayCellClicked(row, this.dbDayKeys[this.currentDayIndex]);
    }

    private String getCurrentDayClass(TimeTableEntity row) {
        switch (this.currentDayIndex) {
            case 0:
                return row.mondayClass;
            case 1:
                return row.tuesdayClass;
            case 2:
                return row.wednesdayClass;
            case 3:
                return row.thursdayClass;
            case 4:
                return row.fridayClass;
            case 5:
                return row.saturdayClass;
            default:
                return "-";
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(1, dp, getResources().getDisplayMetrics());
    }

    private String formatTo12Hour(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            return sdf12.format(date).toLowerCase();
        } catch (Exception e) {
            return time24;
        }
    }

    private void onTimeCellClicked(final TimeTableEntity row) {
        Calendar c = Calendar.getInstance();
        final MaterialTimePicker fromPicker = new MaterialTimePicker.Builder().setTimeFormat(0).setHour(c.get(11)).setMinute(c.get(12)).setTitleText(HttpHeaders.FROM).setInputMode(0).build();
        fromPicker.addOnPositiveButtonClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                TimeTableFragment.this.m312xab93a1c6(fromPicker, row, view);
            }
        });
        fromPicker.show(getParentFragmentManager(), "FROM_PICKER");
    }

    /* JADX INFO: renamed from: lambda$onTimeCellClicked$11$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m312xab93a1c6(MaterialTimePicker fromPicker, final TimeTableEntity row, View view) {
        final int fromHour = fromPicker.getHour();
        final int fromMinute = fromPicker.getMinute();
        final String fromTimeStr = String.format(Locale.getDefault(), "%02d:%02d", Integer.valueOf(fromHour), Integer.valueOf(fromMinute));
        final MaterialTimePicker toPicker = new MaterialTimePicker.Builder().setTimeFormat(0).setHour(fromHour).setMinute(fromMinute).setTitleText("To").setInputMode(0).build();
        toPicker.addOnPositiveButtonClickListener(new View.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                TimeTableFragment.this.m311x36197b85(toPicker, fromHour, fromMinute, row, fromTimeStr, view2);
            }
        });
        toPicker.show(getParentFragmentManager(), "TO_PICKER");
    }

    /* JADX INFO: renamed from: lambda$onTimeCellClicked$10$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m311x36197b85(MaterialTimePicker toPicker, int fromHour, int fromMinute, TimeTableEntity row, String fromTimeStr, View v2) {
        int toHour = toPicker.getHour();
        int toMinute = toPicker.getMinute();
        String toTimeStr = String.format(Locale.getDefault(), "%02d:%02d", Integer.valueOf(toHour), Integer.valueOf(toMinute));
        if (toHour < fromHour || (toHour == fromHour && toMinute <= fromMinute)) {
            Toast.makeText(getContext(), "Invalid Range: 'To' time must be after 'From' time", 1).show();
            return;
        }
        boolean isDuplicate = false;
        Iterator<TimeTableEntity> it = this.currentRows.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            TimeTableEntity item = it.next();
            if (item.id != row.id && fromTimeStr.equals(item.startTime) && toTimeStr.equals(item.endTime)) {
                isDuplicate = true;
                break;
            }
        }
        if (isDuplicate) {
            Toast.makeText(getContext(), "Duplicate Entry: this time slot already exist", 1).show();
            return;
        }
        row.startTime = fromTimeStr;
        row.endTime = toTimeStr;
        this.viewModel.updateTimeTableRow(row);
    }

    private void onDayCellClicked(final TimeTableEntity row, final String dayOfWeek) {
        if (this.classNamesList.isEmpty()) {
            Toast.makeText(getContext(), "Create class first!", 0).show();
        } else {
            final String[] classesArray = (String[]) this.classNamesList.toArray(new String[0]);
            new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "Select Class").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded)).setItems((CharSequence[]) classesArray, new DialogInterface.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda5
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    TimeTableFragment.this.m310x4ec958de(classesArray, dayOfWeek, row, dialogInterface, i);
                }
            }).show();
        }
    }

    /* JADX INFO: renamed from: lambda$onDayCellClicked$12$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m310x4ec958de(java.lang.String[] classesArray, java.lang.String dayKey, com.example.attnd.database.TimeTableEntity row, android.content.DialogInterface dialog, int which) {
        String selectedClass = classesArray[which];
        switch (dayKey) {
            case "Mon": row.mondayClass = selectedClass; break;
            case "Tue": row.tuesdayClass = selectedClass; break;
            case "Wed": row.wednesdayClass = selectedClass; break;
            case "Thu": row.thursdayClass = selectedClass; break;
            case "Fri": row.fridayClass = selectedClass; break;
            case "Sat": row.saturdayClass = selectedClass; break;
        }
        this.viewModel.updateTimeTableRow(row);
    }

    private void showDeleteDialog(final TimeTableEntity row) {
        new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "Delete Row").setMessage((CharSequence) "Do you want to delete this time slot?").setPositiveButton((CharSequence) "Yes", new DialogInterface.OnClickListener() { // from class: com.example.attnd.fragments.TimeTableFragment$$ExternalSyntheticLambda4
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                TimeTableFragment.this.m316xe4912097(row, dialogInterface, i);
            }
        }).setNegativeButton((CharSequence) "No", (DialogInterface.OnClickListener) null).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg_rounded)).show();
    }

    /* JADX INFO: renamed from: lambda$showDeleteDialog$13$com-example-attnd-fragments-TimeTableFragment, reason: not valid java name */
    /* synthetic */ void m316xe4912097(TimeTableEntity row, DialogInterface dialog, int which) {
        this.viewModel.deleteTimeTableRow(row);
    }
}

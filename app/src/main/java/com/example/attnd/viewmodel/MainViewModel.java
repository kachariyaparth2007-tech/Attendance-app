package com.example.attnd.viewmodel;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.attnd.ClassWidgetProvider;
import com.example.attnd.WidgetConfigActivity;
import com.example.attnd.database.AppDatabase;
import com.example.attnd.database.AttendanceDao;
import com.example.attnd.database.AttendanceEntity;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.database.StudentEntity;
import com.example.attnd.database.TimeTableEntity;
import com.example.attnd.model.StudentSummary;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainViewModel extends AndroidViewModel {
    public String currentSelectedClass;
    private AttendanceDao dao;
    private ExecutorService executor;
    private SharedPreferences prefs;
    public Map<String, String> savedAttendanceStatus;
    public String savedSelectedClass;
    public List<String> tempOverallSummaryColumns;
    public List<OverallSummaryRow> tempOverallSummaryData;
    public List<String> tempOverallSummaryHeaders;
    public List<ClassEntity> tempSelectedClassesForSummary;
    public List<String> tempSelectedSubjectsForSummary;

    public interface OnOverallSummaryListener {
        void onError(String error);
        void onMismatch();
        void onSuccess();
    }

    public interface OnSyncListener {
        void onError(String error);
        void onSuccess();
    }

    public interface OnUpdateListener {
        void onError(String error);
        void onSuccess();
    }

    public static class OverallSummaryRow {
        public Map<String, String> attendanceData = new HashMap<>();
        public String name;
        public String rollNo;
    }

    public MainViewModel(Application application) {
        super(application);
        this.savedSelectedClass = null;
        this.savedAttendanceStatus = new HashMap<>();
        this.currentSelectedClass = null;
        this.executor = Executors.newSingleThreadExecutor();
        this.tempOverallSummaryData = new ArrayList<>();
        this.tempOverallSummaryColumns = new ArrayList<>();
        this.tempOverallSummaryHeaders = new ArrayList<>();
        this.tempSelectedClassesForSummary = new ArrayList<>();
        this.tempSelectedSubjectsForSummary = new ArrayList<>();
        AppDatabase db = AppDatabase.getInstance(application);
        this.dao = db.attendanceDao();
        this.prefs = application.getSharedPreferences("AttndPrefs", 0);
    }

    public String getRole() {
        return this.prefs.getString("userRole", "TEACHER");
    }

    public String getUserId() {
        return this.prefs.getString("userId", "");
    }

    public String getPrincipalId() {
        return this.prefs.getString("principalId", "");
    }

    public String getTargetTeacherId() {
        return getUserId();
    }

    public LiveData<List<ClassEntity>> getAllClasses() {
        return this.dao.getAllClasses(getTargetTeacherId());
    }

    public void addClass(final String name, final String subject) {
        this.executor.execute(() -> {
            try {
                String tId = getTargetTeacherId();
                if (tId == null || tId.trim().isEmpty()) {
                    tId = "DEFAULT_ID";
                }
                String upperName = name != null ? name.trim().toUpperCase() : "";
                String upperSubject = subject != null ? subject.trim().toUpperCase() : "";
                this.dao.insertClass(new ClassEntity(upperName, tId, upperSubject));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getApplication(), "Save Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void renameClass(final String oldName, final String newName, final String subject) {
        this.executor.execute(() -> {
            try {
                String tId = getTargetTeacherId();
                if (tId == null || tId.trim().isEmpty()) {
                    tId = "DEFAULT_ID";
                }
                String upperNewName = newName != null ? newName.trim().toUpperCase() : "";
                String upperSubject = subject != null ? subject.trim().toUpperCase() : "";

                // Always update/insert with the new subject
                this.dao.insertClass(new ClassEntity(upperNewName, tId, upperSubject));
                
                if (!oldName.equalsIgnoreCase(upperNewName)) {
                    this.dao.updateStudentClassName(oldName, upperNewName, tId);
                    this.dao.updateAttendanceClassName(oldName, upperNewName, tId);
                    this.dao.updateTimeTableClassName(oldName, upperNewName, tId);
                    this.dao.deleteClass(oldName, tId);
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getApplication(), "Rename Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void deleteClass(final String name) {
        this.executor.execute(() -> {
            try {
                String tId = getTargetTeacherId();
                if (tId == null || tId.trim().isEmpty()) {
                    tId = "DEFAULT_ID";
                }
                this.dao.deleteStudentsByClass(name, tId);
                this.dao.deleteAttendanceByClass(name, tId);
                this.dao.deleteClass(name, tId);
                this.dao.clearDeletedClassFromTimeTable(name, tId);
                SharedPreferences widgetPrefs = getApplication().getSharedPreferences(WidgetConfigActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = widgetPrefs.edit();
                boolean widgetAffected = false;
                for (String key : widgetPrefs.getAll().keySet()) {
                    if (name.equals(widgetPrefs.getString(key, ""))) {
                        editor.remove(key);
                        widgetAffected = true;
                    }
                }
                editor.apply();
                if (widgetAffected) {
                    Intent intent = new Intent(getApplication(), ClassWidgetProvider.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ClassWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    getApplication().sendBroadcast(intent);
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getApplication(), "Delete Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public LiveData<List<StudentEntity>> getStudents(String className) {
        return this.dao.getStudentsForClass(className, getTargetTeacherId());
    }

    public void addStudent(final String name, final String roll, final String className) {
        this.executor.execute(() -> {
            String upperName = name != null ? name.trim().toUpperCase() : "";
            String upperClassName = className != null ? className.trim().toUpperCase() : "";
            this.dao.insertStudent(new StudentEntity(roll, upperName, upperClassName, getTargetTeacherId()));
        });
    }

    public void updateStudent(final StudentEntity oldStudent, final String newName, final String newRoll, final OnUpdateListener listener) {
        final String tId = getTargetTeacherId();
        this.executor.execute(() -> {
            String upperNewName = newName != null ? newName.trim().toUpperCase() : "";
            if (oldStudent.rollNo.equals(newRoll)) {
                this.dao.updateStudentName(oldStudent.className, oldStudent.rollNo, upperNewName, tId);
                listener.onSuccess();
                return;
            }
            int count = this.dao.isStudentExist(oldStudent.className, newRoll, tId);
            if (count > 0) {
                listener.onError("Roll No " + newRoll + " already exists!");
                return;
            }
            this.dao.insertStudent(new StudentEntity(newRoll, upperNewName, oldStudent.className, tId));
            this.dao.updateAttendanceRoll(oldStudent.className, oldStudent.rollNo, newRoll, tId);
            this.dao.deleteStudent(oldStudent);
            listener.onSuccess();
        });
    }

    public void deleteStudent(final StudentEntity student) {
        this.executor.execute(() -> {
            this.dao.deleteStudent(student);
        });
    }

    public LiveData<List<AttendanceEntity>> getDailyAttendance(String className, String date) {
        return this.dao.getAttendanceForDate(className, date, getTargetTeacherId());
    }

    public LiveData<List<AttendanceEntity>> getAttendanceInRange(String className, String fromDate, String toDate) {
        return this.dao.getAttendanceInRangeLive(className, fromDate, toDate, getTargetTeacherId());
    }

    public void saveAttendance(final List<AttendanceEntity> records) {
        this.executor.execute(() -> {
            this.dao.insertAttendance(records);
        });
    }

    public void generateSummary(final String className, final String fromDate, final String toDate, final MutableLiveData<List<StudentSummary>> liveData) {
        final String tId = getTargetTeacherId();
        this.executor.execute(() -> {
            ArrayList<StudentSummary> arrayList = new ArrayList<>();
            List<StudentEntity> students = this.dao.getStudentsListDirect(className, tId);
            List<AttendanceEntity> attendanceList = this.dao.getAttendanceListRange(className, fromDate, toDate, tId);
            for (StudentEntity s : students) {
                StudentSummary summary = new StudentSummary(s.name, s.rollNo);
                if (summary.absentDates == null) {
                    summary.absentDates = new ArrayList<>();
                }
                for (AttendanceEntity a : attendanceList) {
                    if (a.rollNo.equals(s.rollNo)) {
                        summary.totalDays++;
                        if (a.isPresent) {
                            summary.presentDays++;
                        } else {
                            summary.absentDates.add(a.date);
                        }
                    }
                }
                arrayList.add(summary);
            }
            liveData.postValue(arrayList);
        });
    }

    public void getClassNamesDirect(final Consumer<List<String>> callback) {
        final String myId = getUserId();
        this.executor.execute(() -> {
            List<ClassEntity> classes = this.dao.getAllClassesDirect(myId);
            final List<String> names = new ArrayList<>();
            for (ClassEntity c : classes) {
                names.add(c.className);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.accept(names);
            });
        });
    }

    private boolean isFirebaseAvailable() {
        try {
            com.google.firebase.FirebaseApp.getInstance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void uploadDataToFirebase(final List<String> selectedClasses, final OnSyncListener listener) {
        final String myId = getUserId();
        final String pId = getPrincipalId();
        final String myName = this.prefs.getString("userName", "Unknown Teacher");
        
        if (!isFirebaseAvailable()) {
            listener.onError("Firebase not initialized. Check google-services.json.");
            return;
        }

        if (pId.isEmpty()) {
            listener.onError("Principal ID not found. Please update profile.");
        } else {
            this.executor.execute(() -> {
                try {
                    List<ClassEntity> directClasses = this.dao.getSelectedClassesDirect(myId, selectedClasses);
                    List<StudentEntity> directStudents = this.dao.getSelectedStudentsDirect(myId, selectedClasses);
                    List<AttendanceEntity> directAttendance = this.dao.getSelectedAttendanceDirect(myId, selectedClasses);
                    
                    Map<String, Object> syncData = new HashMap<>();
                    syncData.put("timestamp", ServerValue.TIMESTAMP);
                    syncData.put("teacherName", myName);
                    
                    Map<String, Object> classesMap = new HashMap<>();
                    for (ClassEntity ce : directClasses) {
                        Map<String, Object> classData = new HashMap<>();
                        classData.put("subject", ce.subject);
                        
                        List<StudentEntity> classStudents = new ArrayList<>();
                        for (StudentEntity s : directStudents) {
                            if (s.className.equals(ce.className)) classStudents.add(s);
                        }
                        classData.put("students", classStudents);
                        
                        List<AttendanceEntity> classAttendance = new ArrayList<>();
                        for (AttendanceEntity a : directAttendance) {
                            if (a.className.equals(ce.className)) classAttendance.add(a);
                        }
                        classData.put("attendance", classAttendance);
                        
                        classesMap.put(ce.className, classData);
                    }
                    syncData.put("classes", classesMap);
                    
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TemporarySync").child(pId).child(myId);
                    ref.setValue(syncData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Listen for data removal as confirmation that Principal picked it up
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        ref.removeEventListener(this);
                                        listener.onSuccess();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    ref.removeEventListener(this);
                                    listener.onError("Confirmation Failed: " + error.getMessage());
                                }
                            });
                        } else {
                            listener.onError(task.getException() != null ? task.getException().getMessage() : "Unknown Firebase Error");
                        }
                    }).addOnFailureListener(exc -> {
                        listener.onError("Firebase Error: " + exc.getMessage());
                    });
                } catch (Exception e) {
                    listener.onError("App Error: " + e.getMessage());
                }
            });
        }
    }

    public void fetchAllClassesGlobal(final Consumer<List<ClassEntity>> callback) {
        this.executor.execute(() -> {
            try {
                final List<ClassEntity> list = this.dao.getAllClassesGlobalDirect();
                new Handler(Looper.getMainLooper()).post(() -> callback.accept(list));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.accept(new ArrayList<>()));
            }
        });
    }

    public void generateOverallSummary(final List<ClassEntity> selectedClasses, final String fromDate, final String toDate, final OnOverallSummaryListener listener) {
        this.tempSelectedClassesForSummary = selectedClasses;
        this.executor.execute(() -> {
            try {
                if (selectedClasses.size() < 2) {
                    listener.onError("Please select at least 2 classes to compare.");
                    return;
                }
                List<List<StudentEntity>> allStudentsLists = new ArrayList<>();
                for (ClassEntity ce : selectedClasses) {
                    List<StudentEntity> students = this.dao.getStudentsListDirect(ce.className, ce.teacherId);
                    allStudentsLists.add(students);
                }
                List<StudentEntity> baseList = allStudentsLists.get(0);
                boolean isMatch = true;
                for (int i = 1; i < allStudentsLists.size(); i++) {
                    List<StudentEntity> currentList = allStudentsLists.get(i);
                    if (baseList.size() != currentList.size()) {
                        isMatch = false;
                        break;
                    }
                    for (StudentEntity baseStudent : baseList) {
                        boolean found = false;
                        for (StudentEntity curStudent : currentList) {
                            if (baseStudent.rollNo.equals(curStudent.rollNo) && baseStudent.name.trim().equalsIgnoreCase(curStudent.name.trim())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            isMatch = false;
                            break;
                        }
                    }
                    if (!isMatch) break;
                }
                if (!isMatch) {
                    listener.onMismatch();
                    return;
                }
                this.tempOverallSummaryColumns.clear();
                this.tempOverallSummaryHeaders.clear();
                for (int i2 = 0; i2 < selectedClasses.size(); i2++) {
                    this.tempOverallSummaryColumns.add(selectedClasses.get(i2).className);
                    this.tempOverallSummaryHeaders.add(selectedClasses.get(i2).subject);
                }
                List<OverallSummaryRow> rowList = new ArrayList<>();
                for (StudentEntity student : baseList) {
                    OverallSummaryRow row = new OverallSummaryRow();
                    row.rollNo = student.rollNo;
                    row.name = student.name;
                    for (ClassEntity ce2 : selectedClasses) {
                        List<AttendanceEntity> attList = this.dao.getAttendanceListRange(ce2.className, fromDate, toDate, ce2.teacherId);
                        int present = 0;
                        int total = 0;
                        for (AttendanceEntity a : attList) {
                            if (a.rollNo.equals(student.rollNo)) {
                                total++;
                                if (a.isPresent) {
                                    present++;
                                }
                            }
                        }
                        if (total == 0) {
                            row.attendanceData.put(ce2.className, "N/A");
                        } else {
                            int percentage = (present * 100) / total;
                            row.attendanceData.put(ce2.className, percentage + "%");
                        }
                    }
                    rowList.add(row);
                }
                rowList.sort((r1, r2) -> {
                    try {
                        return Integer.compare(Integer.parseInt(r1.rollNo), Integer.parseInt(r2.rollNo));
                    } catch (Exception e) {
                        return r1.rollNo.compareTo(r2.rollNo);
                    }
                });
                this.tempOverallSummaryData = rowList;
                listener.onSuccess();
            } catch (Exception e3) {
                e3.printStackTrace();
                listener.onError("Summary error: " + e3.getMessage());
            }
        });
    }

    public LiveData<List<TimeTableEntity>> getTimeTable() {
        return this.dao.getTimeTable(getTargetTeacherId());
    }

    public void addTimeTableRow(final TimeTableEntity row) {
        this.executor.execute(() -> {
            try {
                this.dao.insertTimeTableRow(row);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplication(), "Add Row Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    public void updateTimeTableRow(final TimeTableEntity row) {
        this.executor.execute(() -> {
            try {
                this.dao.updateTimeTableRow(row);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getApplication(), "Update Row Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    public void deleteTimeTableRow(final TimeTableEntity row) {
        this.executor.execute(() -> {
            this.dao.deleteTimeTableRow(row);
        });
    }
}

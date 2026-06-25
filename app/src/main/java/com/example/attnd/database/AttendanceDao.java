package com.example.attnd.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AttendanceDao {

    @Query("SELECT COUNT(*) FROM classes WHERE className = :name AND teacherId = :tId")
    int checkIfClassExists(String name, String tId);

    @Query("UPDATE timetable SET mondayClass = '-' WHERE mondayClass = :name AND teacherId = :tId")
    void clearMonday(String name, String tId);
    
    @Query("UPDATE timetable SET tuesdayClass = '-' WHERE tuesdayClass = :name AND teacherId = :tId")
    void clearTuesday(String name, String tId);

    @Query("UPDATE timetable SET wednesdayClass = '-' WHERE wednesdayClass = :name AND teacherId = :tId")
    void clearWednesday(String name, String tId);

    @Query("UPDATE timetable SET thursdayClass = '-' WHERE thursdayClass = :name AND teacherId = :tId")
    void clearThursday(String name, String tId);

    @Query("UPDATE timetable SET fridayClass = '-' WHERE fridayClass = :name AND teacherId = :tId")
    void clearFriday(String name, String tId);

    @Query("UPDATE timetable SET saturdayClass = '-' WHERE saturdayClass = :name AND teacherId = :tId")
    void clearSaturday(String name, String tId);

    default void clearDeletedClassFromTimeTable(String name, String tId) {
        clearMonday(name, tId);
        clearTuesday(name, tId);
        clearWednesday(name, tId);
        clearThursday(name, tId);
        clearFriday(name, tId);
        clearSaturday(name, tId);
    }

    @Query("DELETE FROM attendance WHERE teacherId = :tId")
    void deleteAllAttendanceByTeacher(String tId);

    @Query("DELETE FROM classes WHERE teacherId = :tId")
    void deleteAllClassesByTeacher(String tId);

    @Query("DELETE FROM students WHERE teacherId = :tId")
    void deleteAllStudentsByTeacher(String tId);

    @Query("DELETE FROM attendance WHERE className = :className AND teacherId = :tId")
    void deleteAttendanceByClass(String className, String tId);

    @Query("DELETE FROM classes WHERE className = :name AND teacherId = :tId")
    void deleteClass(String name, String tId);

    @Delete
    void deleteStudent(StudentEntity student);

    @Query("DELETE FROM students WHERE className = :className AND teacherId = :tId")
    void deleteStudentsByClass(String className, String tId);

    @Delete
    void deleteTimeTableRow(TimeTableEntity timeTableEntity);

    @Query("SELECT * FROM attendance WHERE teacherId = :tId")
    List<AttendanceEntity> getAllAttendanceDirect(String tId);

    @Query("SELECT * FROM classes WHERE teacherId = :tId")
    LiveData<List<ClassEntity>> getAllClasses(String tId);

    @Query("SELECT * FROM classes WHERE teacherId = :tId")
    List<ClassEntity> getAllClassesDirect(String tId);

    @Query("SELECT * FROM classes")
    List<ClassEntity> getAllClassesGlobalDirect();

    @Query("SELECT * FROM students WHERE teacherId = :tId")
    List<StudentEntity> getAllStudentsDirect(String tId);

    @Query("SELECT * FROM attendance WHERE className = :className AND date = :date AND teacherId = :tId")
    LiveData<List<AttendanceEntity>> getAttendanceForDate(String className, String date, String tId);

    @Query("SELECT * FROM attendance WHERE className = :className AND date BETWEEN :fromDate AND :toDate AND teacherId = :tId")
    List<AttendanceEntity> getAttendanceInRange(String className, String fromDate, String toDate, String tId);

    @Query("SELECT * FROM attendance WHERE className = :className AND date BETWEEN :fromDate AND :toDate AND teacherId = :tId")
    LiveData<List<AttendanceEntity>> getAttendanceInRangeLive(String className, String fromDate, String toDate, String tId);

    @Query("SELECT * FROM attendance WHERE className = :className AND date BETWEEN :fromDate AND :toDate AND teacherId = :tId")
    List<AttendanceEntity> getAttendanceListRange(String className, String fromDate, String toDate, String tId);

    @Query("SELECT * FROM attendance WHERE teacherId = :tId AND className IN (:classNames)")
    List<AttendanceEntity> getSelectedAttendanceDirect(String tId, List<String> classNames);

    @Query("SELECT * FROM classes WHERE teacherId = :tId AND className IN (:classNames)")
    List<ClassEntity> getSelectedClassesDirect(String tId, List<String> classNames);

    @Query("SELECT * FROM classes WHERE className = :name AND teacherId = :tId")
    ClassEntity getClassByName(String name, String tId);

    @Query("SELECT * FROM students WHERE teacherId = :tId AND className IN (:classNames)")
    List<StudentEntity> getSelectedStudentsDirect(String tId, List<String> classNames);

    @Query("SELECT * FROM students WHERE className = :className AND teacherId = :tId")
    LiveData<List<StudentEntity>> getStudentsForClass(String className, String tId);

    @Query("SELECT * FROM students WHERE className = :className AND teacherId = :tId")
    List<StudentEntity> getStudentsListDirect(String className, String tId);

    @Query("SELECT * FROM students WHERE className = :className AND teacherId = :tId")
    List<StudentEntity> getStudentsListNow(String className, String tId);

    @Query("SELECT * FROM timetable WHERE teacherId = :tId ORDER BY startTime ASC")
    LiveData<List<TimeTableEntity>> getTimeTable(String tId);

    @Query("SELECT * FROM timetable WHERE teacherId = :tId ORDER BY startTime ASC")
    List<TimeTableEntity> getTimeTableList(String tId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAttendance(List<AttendanceEntity> records);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClass(ClassEntity classEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudent(StudentEntity student);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTimeTableRow(TimeTableEntity timeTableEntity);

    @Query("SELECT COUNT(*) FROM students WHERE className = :className AND rollNo = :roll AND teacherId = :tId")
    int isStudentExist(String className, String roll, String tId);

    @Query("UPDATE attendance SET className = :newName WHERE className = :oldName AND teacherId = :tId")
    void updateAttendanceClassName(String oldName, String newName, String tId);

    @Query("UPDATE attendance SET rollNo = :newRoll WHERE className = :className AND rollNo = :oldRoll AND teacherId = :tId")
    void updateAttendanceRoll(String className, String oldRoll, String newRoll, String tId);

    @Query("UPDATE students SET className = :newName WHERE className = :oldName AND teacherId = :tId")
    void updateStudentClassName(String oldName, String newName, String tId);

    @Query("UPDATE students SET name = :newName WHERE className = :className AND rollNo = :roll AND teacherId = :tId")
    void updateStudentName(String className, String roll, String newName, String tId);

    @Query("UPDATE timetable SET mondayClass = :newName WHERE mondayClass = :oldName AND teacherId = :tId")
    void updateTimeTableMonday(String oldName, String newName, String tId);

    @Query("UPDATE timetable SET tuesdayClass = :newName WHERE tuesdayClass = :oldName AND teacherId = :tId")
    void updateTimeTableTuesday(String oldName, String newName, String tId);

    @Query("UPDATE timetable SET wednesdayClass = :newName WHERE wednesdayClass = :oldName AND teacherId = :tId")
    void updateTimeTableWednesday(String oldName, String newName, String tId);

    @Query("UPDATE timetable SET thursdayClass = :newName WHERE thursdayClass = :oldName AND teacherId = :tId")
    void updateTimeTableThursday(String oldName, String newName, String tId);

    @Query("UPDATE timetable SET fridayClass = :newName WHERE fridayClass = :oldName AND teacherId = :tId")
    void updateTimeTableFriday(String oldName, String newName, String tId);

    @Query("UPDATE timetable SET saturdayClass = :newName WHERE saturdayClass = :oldName AND teacherId = :tId")
    void updateTimeTableSaturday(String oldName, String newName, String tId);

    default void updateTimeTableClassName(String oldName, String newName, String tId) {
        updateTimeTableMonday(oldName, newName, tId);
        updateTimeTableTuesday(oldName, newName, tId);
        updateTimeTableWednesday(oldName, newName, tId);
        updateTimeTableThursday(oldName, newName, tId);
        updateTimeTableFriday(oldName, newName, tId);
        updateTimeTableSaturday(oldName, newName, tId);
    }

    @Update
    void updateTimeTableRow(TimeTableEntity timeTableEntity);
}

package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {

    List<Attendance> findByRoomCodeAndAttendanceDate(
            String roomCode,
            LocalDate attendanceDate
    );

    void deleteByRoomCodeAndAttendanceDate(
            String roomCode,
            LocalDate attendanceDate
    );
}
package roomapp;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String roomCode;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private boolean present;

    public Attendance() {
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public boolean isPresent() {
        return present;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
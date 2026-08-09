package roomapp;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false, unique = true)
    private String roomCode;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String adminPin;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getAdminPin() {
        return adminPin;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public void setAdminPin(String adminPin) {
        this.adminPin = adminPin;
    }
}
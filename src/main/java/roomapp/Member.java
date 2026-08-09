package roomapp;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String roomCode;

    @Column(nullable = false)
    private boolean active = true;

    // Database compatibility kosam matrame
    // App lo andariki equal rights
    @Column(nullable = false)
    private boolean admin = false;

    @Column(nullable = false)
    private boolean presentToday = false;

    public Member() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isPresentToday() {
        return presentToday;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setPresentToday(boolean presentToday) {
        this.presentToday = presentToday;
    }
}
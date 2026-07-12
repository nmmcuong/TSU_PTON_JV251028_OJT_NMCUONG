package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", nullable = false, length = 50)
    private String roomName;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "seats_x")
    private Integer seatsX;

    @Column(name = "seats_y")
    private Integer seatsY;

    // Ánh xạ mảng tọa độ thành kiểu TEXT trong DB để lưu chuỗi cấu hình cấu trúc ghế
    @Column(name = "vip_seats", columnDefinition = "TEXT")
    private String vipSeats;

    @Column(name = "couple_seats", columnDefinition = "TEXT")
    private String coupleSeats;

    private Boolean status;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Showtime> showtimes;
    
    public Room() {}

    // --- GETTER & SETTER ---
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getSeatsX() { return seatsX; }
    public void setSeatsX(Integer seatsX) { this.seatsX = seatsX; }

    public Integer getSeatsY() { return seatsY; }
    public void setSeatsY(Integer seatsY) { this.seatsY = seatsY; }

    public String getVipSeats() { return vipSeats; }
    public void setVipSeats(String vipSeats) { this.vipSeats = vipSeats; }

    public String getCoupleSeats() { return coupleSeats; }
    public void setCoupleSeats(String coupleSeats) { this.coupleSeats = coupleSeats; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public List<Showtime> getShowtimes() { return showtimes; }
    public void setShowtimes(List<Showtime> showtimes) { this.showtimes = showtimes; }
}

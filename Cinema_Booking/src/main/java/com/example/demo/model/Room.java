package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}

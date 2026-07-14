package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho một ghế cụ thể trong phòng chiếu phim.
 * Dữ liệu này được SEED sẵn (hardcode) khi khởi động ứng dụng.
 * Mỗi phòng chiếu có một bộ ghế cố định.
 */
@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "seat_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    // Quan hệ N-1: Nhiều ghế thuộc 1 phòng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    /**
     * Mã ghế dạng "A1", "B5", "C10"...
     * Quy ước: Hàng = chữ cái (A, B, C...), Cột = số (1, 2, 3...)
     */
    @Column(name = "seat_code", nullable = false, length = 10)
    private String seatCode;

    /**
     * Loại ghế: NORMAL (thường), VIP, COUPLE (cặp đôi)
     */
    @Column(name = "seat_type", length = 20)
    private String seatType;

    /**
     * Constructor mặc định bắt buộc cho JPA
     */
    public Seat() {
    }

    /**
     * Constructor tiện lợi để dùng khi seed data
     */
    public Seat(Room room, String seatCode, String seatType) {
        this.room = room;
        this.seatCode = seatCode;
        this.seatType = seatType;
    }

	public Long getSeatId() {
		return seatId;
	}

	public void setSeatId(Long seatId) {
		this.seatId = seatId;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getSeatType() {
		return seatType;
	}

	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}
}

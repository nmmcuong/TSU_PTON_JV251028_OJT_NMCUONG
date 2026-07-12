package com.example.demo.enums;

/**
 * Phân loại vai trò người dùng trong hệ thống.
 * CORE-02: Phân quyền dựa trên Role này.
 *
 * - CUSTOMER : Khách hàng đặt vé, xem lịch sử
 * - STAFF    : Nhân viên tra cứu đơn hàng, in vé tại quầy
 * - ADMIN    : Quản trị viên toàn quyền
 */
public enum Role {
    CUSTOMER,
    STAFF,
    ADMIN
}

package com.managementbanhxeo.dao;

import com.managementbanhxeo.config.DBConfig;
import com.managementbanhxeo.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User getUserByUsernameAndPassword(String username, String password) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getString("role"), rs.getString("email"), rs.getTimestamp("created_at"));
            }
            System.out.println("Kiểm tra đăng nhập cho: " + username);
        } catch (SQLException e) {
            System.out.println("Lỗi: Kiểm tra đăng nhập thất bại - " + e.getMessage());
        }
        return user;
    }

    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đăng ký thành công cho: " + username);
                return true;
            } else {
                System.out.println("Đăng ký thất bại cho: " + username + " - Không có dòng nào được thêm.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Lỗi: Đăng ký thất bại - " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers(int page, int pageSize) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getString("role"), rs.getString("email"), rs.getTimestamp("created_at")));
            }
            System.out.println("Lấy danh sách người dùng thành công!");
        } catch (SQLException e) {
            System.out.println("Lỗi: Lấy danh sách người dùng thất bại - " + e.getMessage());
        }
        return users;
    }

    public int getTotalUsers() {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi: Đếm tổng số người dùng thất bại - " + e.getMessage());
        }
        return total;
    }

    public User getUserById(int userId) {
        User user = null;
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getString("role"), rs.getString("email"), rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi: Lấy người dùng theo ID thất bại - " + e.getMessage());
        }
        return user;
    }

    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            String role = user.getRole();
            if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
                role = "USER"; // Mặc định là USER nếu role không hợp lệ
            }
            ps.setString(4, role);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Thêm người dùng thành công!");
            } else {
                throw new SQLException("Thêm người dùng thất bại: Không có hàng nào bị ảnh hưởng.");
            }
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            String role = user.getRole();
            if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
                role = "USER"; // Mặc định là USER nếu role không hợp lệ
            }
            ps.setString(4, role);
            ps.setInt(5, user.getUserId());
            ps.executeUpdate();
            System.out.println("Cập nhật người dùng thành công!");
        } catch (SQLException e) {
            System.out.println("Lỗi: Cập nhật người dùng thất bại - " + e.getMessage());
        }
    }

    public void deleteUser(int userId) throws SQLException {
        String sqlDeleteOrders = "DELETE FROM orders WHERE user_id = ?";
        String sqlDeleteUser = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = DBConfig.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Xóa các đơn hàng liên quan trước
            try (PreparedStatement psOrders = conn.prepareStatement(sqlDeleteOrders)) {
                psOrders.setInt(1, userId);
                psOrders.executeUpdate();
            }

            // Xóa người dùng
            try (PreparedStatement psUser = conn.prepareStatement(sqlDeleteUser)) {
                psUser.setInt(1, userId);
                int rowsAffected = psUser.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Xóa người dùng thất bại: Không tìm thấy user_id " + userId);
                }
                System.out.println("Xóa người dùng thành công!");
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback nếu có lỗi
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
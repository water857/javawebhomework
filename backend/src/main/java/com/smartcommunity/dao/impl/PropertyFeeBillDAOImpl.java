package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.PropertyFeeBillDAO;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PropertyFeeBillDAOImpl implements PropertyFeeBillDAO {
    @Override
    public int addBill(PropertyFeeBill bill) {
        String sql = "INSERT INTO property_fee_bill (user_id, period_start, period_end, amount, status, items, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, bill.getUserId());
            pstmt.setDate(2, new java.sql.Date(bill.getPeriodStart().getTime()));
            pstmt.setDate(3, new java.sql.Date(bill.getPeriodEnd().getTime()));
            pstmt.setDouble(4, bill.getAmount());
            pstmt.setString(5, bill.getStatus());
            pstmt.setString(6, bill.getItems());
            pstmt.setDate(7, new java.sql.Date(bill.getDueDate().getTime()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add property fee bill", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public int batchAddBills(List<PropertyFeeBill> bills) {
        String sql = "INSERT INTO property_fee_bill (user_id, period_start, period_end, amount, status, items, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);

            for (PropertyFeeBill bill : bills) {
                pstmt.setInt(1, bill.getUserId());
                pstmt.setDate(2, new java.sql.Date(bill.getPeriodStart().getTime()));
                pstmt.setDate(3, new java.sql.Date(bill.getPeriodEnd().getTime()));
                pstmt.setDouble(4, bill.getAmount());
                pstmt.setString(5, bill.getStatus());
                pstmt.setString(6, bill.getItems());
                pstmt.setDate(7, new java.sql.Date(bill.getDueDate().getTime()));
                pstmt.addBatch();
            }

            int[] affectedRows = pstmt.executeBatch();
            conn.commit();

            int totalAffected = 0;
            for (int affected : affectedRows) {
                totalAffected += affected;
            }

            return totalAffected;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to batch add property fee bills", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public PropertyFeeBill getBillById(int id) {
        String sql = "SELECT * FROM property_fee_bill WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBill(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee bill by id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeeBill> getBillsByUserId(int userId) {
        String sql = "SELECT * FROM property_fee_bill WHERE user_id = ? ORDER BY period_start DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee bills by user id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeeBill> getBillsByUserIdAndStatus(int userId, String status) {
        String sql = "SELECT * FROM property_fee_bill WHERE user_id = ? AND status = ? ORDER BY period_start DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee bills by user id and status", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeeBill> getBillsByCondition(int userId, String status, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM property_fee_bill WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND period_start >= ?");
            params.add(startDate);
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND period_end <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY period_start DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pstmt.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                }
            }

            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee bills by condition", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public int updateBill(PropertyFeeBill bill) {
        String sql = "UPDATE property_fee_bill SET period_start = ?, period_end = ?, amount = ?, status = ?, items = ?, due_date = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, new java.sql.Date(bill.getPeriodStart().getTime()));
            pstmt.setDate(2, new java.sql.Date(bill.getPeriodEnd().getTime()));
            pstmt.setDouble(3, bill.getAmount());
            pstmt.setString(4, bill.getStatus());
            pstmt.setString(5, bill.getItems());
            pstmt.setDate(6, new java.sql.Date(bill.getDueDate().getTime()));
            pstmt.setInt(7, bill.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update property fee bill", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public int updateBillStatus(int billId, String status) {
        String sql = "UPDATE property_fee_bill SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update property fee bill status", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public int markReminderSent(int billId) {
        String sql = "UPDATE property_fee_bill SET payment_deadline_reminder_sent = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, 1); // 使用1表示true，适配MySQL的TINYINT(1)类型
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to mark reminder as sent", e);
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<PropertyFeeBill> getAllBills(String status, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM property_fee_bill");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" WHERE status = ?");
            params.add(status);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE" : " AND");
            sql.append(" period_start >= ?");
            params.add(startDate);
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE" : " AND");
            sql.append(" period_end <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY period_start DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // 设置参数，根据参数类型选择合适的set方法
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pstmt.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                }
            }

            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get all property fee bills", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public Object[] getBillStatistics() {
        String sql = "SELECT COUNT(*) as total_bills, SUM(CASE WHEN status = 'paid' THEN amount ELSE 0 END) as paid_amount, SUM(CASE WHEN status != 'paid' THEN amount ELSE 0 END) as unpaid_amount, COUNT(CASE WHEN status = 'paid' THEN 1 END) as paid_count, COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue_count, COUNT(CASE WHEN status = 'unpaid' THEN 1 END) as unpaid_count, COUNT(CASE WHEN status = 'partially_paid' THEN 1 END) as partially_paid_count FROM property_fee_bill";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Object[] stats = new Object[7];
                stats[0] = rs.getInt("total_bills");
                stats[1] = rs.getDouble("paid_amount");
                stats[2] = rs.getDouble("unpaid_amount");
                stats[3] = rs.getInt("paid_count");
                stats[4] = rs.getInt("overdue_count");
                stats[5] = rs.getInt("unpaid_count");
                stats[6] = rs.getInt("partially_paid_count");
                return stats;
            }
            return new Object[7];
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get bill statistics", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public int deleteBill(int billId) {
        String sql = "DELETE FROM property_fee_bill WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, billId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete property fee bill", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public List<PropertyFeeBill> getOverdueBills() {
        String sql = "SELECT * FROM property_fee_bill WHERE status = 'unpaid' AND due_date < CURDATE() ORDER BY due_date ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get overdue property fee bills", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    private List<PropertyFeeBill> mapResultSetToList(ResultSet rs) throws SQLException {
        List<PropertyFeeBill> bills = new ArrayList<>();
        while (rs.next()) {
            bills.add(mapResultSetToBill(rs));
        }
        return bills;
    }

    private PropertyFeeBill mapResultSetToBill(ResultSet rs) throws SQLException {
        PropertyFeeBill bill = new PropertyFeeBill();
        
        // 检查并设置所有字段，确保只有在字段存在时才访问
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // 创建字段存在映射
        boolean hasId = false;
        boolean hasUserId = false;
        boolean hasPeriodStart = false;
        boolean hasPeriodEnd = false;
        boolean hasAmount = false;
        boolean hasStatus = false;
        boolean hasItems = false;
        boolean hasDueDate = false;
        boolean hasCreatedAt = false;
        boolean hasUpdatedAt = false;
        boolean hasPaymentDeadlineReminderSent = false;
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            switch (columnName) {
                case "id":
                    hasId = true;
                    break;
                case "user_id":
                    hasUserId = true;
                    break;
                case "period_start":
                    hasPeriodStart = true;
                    break;
                case "period_end":
                    hasPeriodEnd = true;
                    break;
                case "amount":
                    hasAmount = true;
                    break;
                case "status":
                    hasStatus = true;
                    break;
                case "items":
                    hasItems = true;
                    break;
                case "due_date":
                    hasDueDate = true;
                    break;
                case "payment_deadline_reminder_sent":
                    hasPaymentDeadlineReminderSent = true;
                    break;
                case "created_at":
                    hasCreatedAt = true;
                    break;
                case "updated_at":
                    hasUpdatedAt = true;
                    break;
            }
        }
        
        // 仅在字段存在时设置值
        if (hasId) {
            bill.setId(rs.getInt("id"));
        }
        if (hasUserId) {
            bill.setUserId(rs.getInt("user_id"));
        }
        if (hasPeriodStart) {
            bill.setPeriodStart(rs.getDate("period_start"));
        }
        if (hasPeriodEnd) {
            bill.setPeriodEnd(rs.getDate("period_end"));
        }
        if (hasAmount) {
            bill.setAmount(rs.getDouble("amount"));
        }
        if (hasStatus) {
            bill.setStatus(rs.getString("status"));
        }
        if (hasItems) {
            // 处理items字段可能为null的情况
            bill.setItems(rs.getString("items"));
        }
        if (hasDueDate) {
            bill.setDueDate(rs.getDate("due_date"));
        }
        if (hasPaymentDeadlineReminderSent) {
            bill.setPaymentDeadlineReminderSent(rs.getBoolean("payment_deadline_reminder_sent"));
        }
        if (hasCreatedAt) {
            bill.setCreatedAt(rs.getTimestamp("created_at"));
        }
        if (hasUpdatedAt) {
            bill.setUpdatedAt(rs.getTimestamp("updated_at"));
        }
        
        return bill;
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

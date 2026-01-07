package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.PropertyFeePaymentDAO;
import com.smartcommunity.entity.PropertyFeePayment;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyFeePaymentDAOImpl implements PropertyFeePaymentDAO {
    @Override
    public int addPayment(PropertyFeePayment payment) {
        String sql = "INSERT INTO property_fee_payment (bill_id, user_id, amount, payment_method, transaction_id, status, payment_time, auto_deduction) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, payment.getBillId());
            pstmt.setInt(2, payment.getUserId());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentMethod());
            pstmt.setString(5, payment.getTransactionId());
            pstmt.setString(6, payment.getStatus());
            if (payment.getPaymentTime() != null) {
                pstmt.setTimestamp(7, new java.sql.Timestamp(payment.getPaymentTime().getTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }
            pstmt.setBoolean(8, payment.isAutoDeduction());
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
            throw new RuntimeException("Failed to add property fee payment", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public PropertyFeePayment getPaymentById(int id) {
        String sql = "SELECT * FROM property_fee_payment WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPayment(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee payment by id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByBillId(int billId) {
        String sql = "SELECT * FROM property_fee_payment WHERE bill_id = ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, billId);
            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee payments by bill id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByUserId(int userId) {
        String sql = "SELECT * FROM property_fee_payment WHERE user_id = ? ORDER BY created_at DESC";
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
            throw new RuntimeException("Failed to get property fee payments by user id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByCondition(int userId, String paymentMethod, String startDate, String endDate, Double minAmount, Double maxAmount) {
        StringBuilder sql = new StringBuilder("SELECT * FROM property_fee_payment WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append(" AND payment_method = ?");
            params.add(paymentMethod);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND created_at >= ?");
            params.add(startDate);
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND created_at <= ?");
            params.add(endDate);
        }

        if (minAmount != null) {
            sql.append(" AND amount >= ?");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append(" AND amount <= ?");
            params.add(maxAmount);
        }

        sql.append(" ORDER BY created_at DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pstmt.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                }
            }

            rs = pstmt.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee payments by condition", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public PropertyFeePayment getPaymentByTransactionId(String transactionId) {
        String sql = "SELECT * FROM property_fee_payment WHERE transaction_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, transactionId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPayment(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get property fee payment by transaction id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public int updatePayment(PropertyFeePayment payment) {
        String sql = "UPDATE property_fee_payment SET bill_id = ?, user_id = ?, amount = ?, payment_method = ?, transaction_id = ?, status = ?, payment_time = ?, auto_deduction = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, payment.getBillId());
            pstmt.setInt(2, payment.getUserId());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentMethod());
            pstmt.setString(5, payment.getTransactionId());
            pstmt.setString(6, payment.getStatus());
            if (payment.getPaymentTime() != null) {
                pstmt.setTimestamp(7, new java.sql.Timestamp(payment.getPaymentTime().getTime()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }
            pstmt.setBoolean(8, payment.isAutoDeduction());
            pstmt.setInt(9, payment.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update property fee payment", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public int updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE property_fee_payment SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, paymentId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update property fee payment status", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, null);
        }
    }

    @Override
    public List<PropertyFeePayment> getAllPayments(String paymentMethod, String status, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM property_fee_payment");
        List<Object> params = new ArrayList<>();

        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            sql.append(" WHERE payment_method = ?");
            params.add(paymentMethod);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE" : " AND");
            sql.append(" status = ?");
            params.add(status);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE" : " AND");
            sql.append(" created_at >= ?");
            params.add(startDate);
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE" : " AND");
            sql.append(" created_at <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY created_at DESC");

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
            throw new RuntimeException("Failed to get all property fee payments", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public Object[] getPaymentStatistics() {
        String sql = "SELECT COUNT(*) as total_payments, SUM(amount) as total_amount, COUNT(CASE WHEN status = 'success' THEN 1 END) as successful_count, SUM(CASE WHEN status = 'success' THEN amount ELSE 0 END) as successful_amount, COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_count FROM property_fee_payment";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Object[] stats = new Object[5];
                stats[0] = rs.getInt("total_payments");
                stats[1] = rs.getDouble("total_amount");
                stats[2] = rs.getInt("successful_count");
                stats[3] = rs.getDouble("successful_amount");
                stats[4] = rs.getInt("failed_count");
                return stats;
            }
            return new Object[5];
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get payment statistics", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public List<PropertyFeePayment> getAbnormalPayments() {
        String sql = "SELECT * FROM property_fee_payment WHERE status = 'failed' OR (status = 'pending' AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)) ORDER BY created_at DESC";
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
            throw new RuntimeException("Failed to get abnormal property fee payments", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    private List<PropertyFeePayment> mapResultSetToList(ResultSet rs) throws SQLException {
        List<PropertyFeePayment> payments = new ArrayList<>();
        while (rs.next()) {
            payments.add(mapResultSetToPayment(rs));
        }
        return payments;
    }

    private PropertyFeePayment mapResultSetToPayment(ResultSet rs) throws SQLException {
        PropertyFeePayment payment = new PropertyFeePayment();
        payment.setId(rs.getInt("id"));
        payment.setBillId(rs.getInt("bill_id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setStatus(rs.getString("status"));
        payment.setPaymentTime(rs.getTimestamp("payment_time"));
        payment.setAutoDeduction(rs.getBoolean("auto_deduction"));
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        payment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return payment;
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

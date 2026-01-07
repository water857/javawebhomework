package com.smartcommunity.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection and queries...");
        
        try {
            // Test database connection
            Connection conn = DBUtil.getConnection();
            System.out.println("✓ Database connection established");
            
            // Test property_fee_bill count
            String billCountSql = "SELECT COUNT(*) FROM property_fee_bill";
            PreparedStatement billCountStmt = conn.prepareStatement(billCountSql);
            ResultSet billCountRs = billCountStmt.executeQuery();
            if (billCountRs.next()) {
                int billCount = billCountRs.getInt(1);
                System.out.println("✓ property_fee_bill count: " + billCount);
            }
            billCountStmt.close();
            billCountRs.close();
            
            // Test user count with role=resident
            String userCountSql = "SELECT COUNT(*) FROM user WHERE role = 'resident'";
            PreparedStatement userCountStmt = conn.prepareStatement(userCountSql);
            ResultSet userCountRs = userCountStmt.executeQuery();
            if (userCountRs.next()) {
                int userCount = userCountRs.getInt(1);
                System.out.println("✓ resident user count: " + userCount);
            }
            userCountStmt.close();
            userCountRs.close();
            
            // Test property_fee_payment count
            String paymentCountSql = "SELECT COUNT(*) FROM property_fee_payment";
            PreparedStatement paymentCountStmt = conn.prepareStatement(paymentCountSql);
            ResultSet paymentCountRs = paymentCountStmt.executeQuery();
            if (paymentCountRs.next()) {
                int paymentCount = paymentCountRs.getInt(1);
                System.out.println("✓ property_fee_payment count: " + paymentCount);
            }
            paymentCountStmt.close();
            paymentCountRs.close();
            
            // Test bill statistics query
            String billStatsSql = "SELECT COUNT(*) as total_bills, SUM(CASE WHEN status = 'paid' THEN amount ELSE 0 END) as paid_amount, SUM(CASE WHEN status != 'paid' THEN amount ELSE 0 END) as unpaid_amount, COUNT(CASE WHEN status = 'paid' THEN 1 END) as paid_count, COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue_count, COUNT(CASE WHEN status = 'unpaid' THEN 1 END) as unpaid_count, COUNT(CASE WHEN status = 'partially_paid' THEN 1 END) as partially_paid_count FROM property_fee_bill";
            PreparedStatement billStatsStmt = conn.prepareStatement(billStatsSql);
            ResultSet billStatsRs = billStatsStmt.executeQuery();
            if (billStatsRs.next()) {
                System.out.println("✓ Bill statistics:");
                System.out.println("  Total bills: " + billStatsRs.getInt("total_bills"));
                System.out.println("  Paid amount: " + billStatsRs.getDouble("paid_amount"));
                System.out.println("  Unpaid amount: " + billStatsRs.getDouble("unpaid_amount"));
                System.out.println("  Paid count: " + billStatsRs.getInt("paid_count"));
                System.out.println("  Overdue count: " + billStatsRs.getInt("overdue_count"));
                System.out.println("  Unpaid count: " + billStatsRs.getInt("unpaid_count"));
                System.out.println("  Partially paid count: " + billStatsRs.getInt("partially_paid_count"));
            }
            billStatsStmt.close();
            billStatsRs.close();
            
            // Test payment statistics query
            String paymentStatsSql = "SELECT COUNT(*) as total_payments, SUM(amount) as total_amount, COUNT(CASE WHEN status = 'success' THEN 1 END) as successful_count, SUM(CASE WHEN status = 'success' THEN amount ELSE 0 END) as successful_amount, COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_count FROM property_fee_payment";
            PreparedStatement paymentStatsStmt = conn.prepareStatement(paymentStatsSql);
            ResultSet paymentStatsRs = paymentStatsStmt.executeQuery();
            if (paymentStatsRs.next()) {
                System.out.println("✓ Payment statistics:");
                System.out.println("  Total payments: " + paymentStatsRs.getInt("total_payments"));
                System.out.println("  Total amount: " + paymentStatsRs.getDouble("total_amount"));
                System.out.println("  Successful count: " + paymentStatsRs.getInt("successful_count"));
                System.out.println("  Successful amount: " + paymentStatsRs.getDouble("successful_amount"));
                System.out.println("  Failed count: " + paymentStatsRs.getInt("failed_count"));
            }
            paymentStatsStmt.close();
            paymentStatsRs.close();
            
            // Close connection
            DBUtil.closeConnection(conn);
            System.out.println("✓ Database connection closed");
            
        } catch (SQLException e) {
            System.out.println("✗ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
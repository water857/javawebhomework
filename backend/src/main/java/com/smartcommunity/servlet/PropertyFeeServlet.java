package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.entity.PropertyFeePayment;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.PropertyFeeBillService;
import com.smartcommunity.service.PropertyFeePaymentService;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.PropertyFeeBillServiceImpl;
import com.smartcommunity.service.impl.PropertyFeePaymentServiceImpl;
import com.smartcommunity.service.impl.UserServiceImpl;
import com.smartcommunity.util.RoleConstants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PropertyFeeServlet extends HttpServlet {
    private PropertyFeeBillService billService = new PropertyFeeBillServiceImpl();
    private PropertyFeePaymentService paymentService = new PropertyFeePaymentServiceImpl();
    private UserService userService = new UserServiceImpl();
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            
            // 获取当前用户信息
            String username = (String) req.getAttribute("username");
            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "Unauthorized")));
                return;
            }
            User currentUser = userService.getUserByUsername(username);
            int userId = currentUser.getId();
            
            // 居民端API
            if (relativeURI.equals("/api/property-fee/bills")) {
                // 获取账单列表
                String status = req.getParameter("status");
                String startDate = req.getParameter("startDate");
                String endDate = req.getParameter("endDate");
                
                List<PropertyFeeBill> bills = billService.getBillsByCondition(userId, status, startDate, endDate);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Bills retrieved successfully", bills)));
                return;
            } else if (relativeURI.startsWith("/api/property-fee/bills/")) {
                // 获取账单详情
                String[] parts = relativeURI.split("/");
                if (parts.length != 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid bill ID")));
                    return;
                }
                int billId = Integer.parseInt(parts[4]);
                
                PropertyFeeBill bill = billService.getBillById(billId);
                if (bill == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "Bill not found")));
                    return;
                }
                
                // 验证账单属于当前用户
                if (bill.getUserId() != userId && !currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                // 获取居民信息
                User resident = userService.getUserById(bill.getUserId());
                
                // 创建包含居民信息的响应对象
                BillDetailResponse billDetail = new BillDetailResponse();
                billDetail.setBill(bill);
                billDetail.setResident(resident);
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Bill retrieved successfully", billDetail)));
                return;
            } else if (relativeURI.equals("/api/property-fee/payments")) {
                // 获取缴费记录
                String paymentMethod = req.getParameter("paymentMethod");
                String startDate = req.getParameter("startDate");
                String endDate = req.getParameter("endDate");
                String minAmountStr = req.getParameter("minAmount");
                String maxAmountStr = req.getParameter("maxAmount");
                
                Double minAmount = null;
                Double maxAmount = null;
                if (minAmountStr != null && !minAmountStr.isEmpty()) {
                    minAmount = Double.parseDouble(minAmountStr);
                }
                if (maxAmountStr != null && !maxAmountStr.isEmpty()) {
                    maxAmount = Double.parseDouble(maxAmountStr);
                }
                
                List<PropertyFeePayment> payments = paymentService.getPaymentsByCondition(userId, paymentMethod, startDate, endDate, minAmount, maxAmount);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Payments retrieved successfully", payments)));
                return;
            }
            
            // 物业管理员端API
            if (relativeURI.equals("/api/admin/property-fee/bills")) {
                // 管理员获取所有账单
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                String status = req.getParameter("status");
                String startDate = req.getParameter("startDate");
                String endDate = req.getParameter("endDate");
                
                List<PropertyFeeBill> bills = billService.getAllBills(status, startDate, endDate);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Bills retrieved successfully", bills)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/payments")) {
                // 管理员获取所有支付记录
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                String paymentMethod = req.getParameter("paymentMethod");
                String status = req.getParameter("status");
                String startDate = req.getParameter("startDate");
                String endDate = req.getParameter("endDate");
                
                List<PropertyFeePayment> payments = paymentService.getAllPayments(paymentMethod, status, startDate, endDate);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Payments retrieved successfully", payments)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/statistics")) {
                // 获取收费统计数据
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                // 获取统计数据
                Object[] billStats = billService.getBillStatistics();
                Object[] paymentStats = paymentService.getPaymentStatistics();
                
                // 创建统计响应对象
                StatisticsResponse statsResponse = new StatisticsResponse();
                
                // 基本账单统计
                statsResponse.setTotalBills((int) billStats[0]);
                statsResponse.setTotalPaidAmount((double) billStats[1]);
                statsResponse.setTotalUnpaidAmount((double) billStats[2]);
                statsResponse.setPaidCount((int) billStats[3]);
                statsResponse.setOverdueCount((int) billStats[4]);
                statsResponse.setUnpaidCount((int) billStats[5]);
                statsResponse.setPartiallyPaidCount((int) billStats[6]);
                
                // 支付统计
                statsResponse.setTotalPayments((int) paymentStats[0]);
                statsResponse.setTotalPaymentAmount((double) paymentStats[1]);
                
                // 计算平均缴费金额
                double averagePayment = paymentStats[0] != null && (int) paymentStats[0] > 0 
                    ? (double) paymentStats[1] / (int) paymentStats[0] 
                    : 0;
                statsResponse.setAveragePaymentAmount(averagePayment);
                
                // 从数据库查询真实住户数据
                Connection conn = null;
                PreparedStatement totalHouseholdsStmt = null;
                ResultSet totalHouseholdsRs = null;
                PreparedStatement paidHouseholdsStmt = null;
                ResultSet paidHouseholdsRs = null;
                
                try {
                    conn = com.smartcommunity.util.DBUtil.getConnection();
                    
                    // 1. 查询总住户数：统计居民用户数量
                    String totalHouseholdsSql = "SELECT COUNT(*) FROM user WHERE role = 'resident'"; 
                    totalHouseholdsStmt = conn.prepareStatement(totalHouseholdsSql);
                    totalHouseholdsRs = totalHouseholdsStmt.executeQuery();
                    int totalHouseholds = 0;
                    if (totalHouseholdsRs.next()) {
                        totalHouseholds = totalHouseholdsRs.getInt(1);
                    }
                    statsResponse.setTotalHouseholds(totalHouseholds);
                    
                    // 2. 查询已缴费户数：统计至少有一个已缴费账单的住户数量
                    String paidHouseholdsSql = "SELECT COUNT(DISTINCT user_id) FROM property_fee_bill WHERE status = 'paid'"; 
                    paidHouseholdsStmt = conn.prepareStatement(paidHouseholdsSql);
                    paidHouseholdsRs = paidHouseholdsStmt.executeQuery();
                    int paidHouseholds = 0;
                    if (paidHouseholdsRs.next()) {
                        paidHouseholds = paidHouseholdsRs.getInt(1);
                    }
                    statsResponse.setPaidHouseholds(paidHouseholds);
                    
                    // 3. 计算未缴费户数
                    int unpaidHouseholds = totalHouseholds - paidHouseholds;
                    statsResponse.setUnpaidHouseholds(unpaidHouseholds);
                    
                    // 4. 计算缴费率
                    double paymentRate = totalHouseholds > 0 
                        ? (double) paidHouseholds / totalHouseholds * 100 
                        : 0;
                    statsResponse.setPaymentRate(paymentRate);
                    
                    // 5. 查询未缴费用户列表
                    // 获取筛选参数
                    String periodFilter = req.getParameter("period");
                    String startDateFilter = req.getParameter("startDate");
                    String endDateFilter = req.getParameter("endDate");
                    String buildingFilter = req.getParameter("building");
                    
                    // 构建查询SQL，包含筛选条件
                    StringBuilder unpaidUsersSql = new StringBuilder(
                        "SELECT u.id as user_id, u.real_name as user_name, " +
                        "SUBSTRING_INDEX(u.address, '-', 1) as building, " +
                        "SUBSTRING_INDEX(u.address, '-', -1) as room_number, " +
                        "CONCAT(YEAR(b.period_start), '-', LPAD(MONTH(b.period_start), 2, '0')) as period, " +
                        "b.amount, " +
                        "DATEDIFF(CURRENT_DATE(), b.due_date) as overdue_days " +
                        "FROM property_fee_bill b " +
                        "JOIN user u ON b.user_id = u.id " +
                        "WHERE b.status IN ('unpaid', 'overdue') "
                    );
                    
                    // 添加筛选条件
                    if (periodFilter != null && !periodFilter.isEmpty()) {
                        unpaidUsersSql.append(" AND CONCAT(YEAR(b.period_start), '-', LPAD(MONTH(b.period_start), 2, '0')) = ? ");
                    }
                    if (startDateFilter != null && !startDateFilter.isEmpty()) {
                        unpaidUsersSql.append(" AND b.period_start >= ? ");
                    }
                    if (endDateFilter != null && !endDateFilter.isEmpty()) {
                        unpaidUsersSql.append(" AND b.period_end <= ? ");
                    }
                    if (buildingFilter != null && !buildingFilter.isEmpty()) {
                        unpaidUsersSql.append(" AND SUBSTRING_INDEX(u.address, '-', 1) = ? ");
                    }
                    
                    // 添加排序
                    unpaidUsersSql.append(" ORDER BY overdue_days DESC, u.id ASC");
                    
                    PreparedStatement unpaidUsersStmt = conn.prepareStatement(unpaidUsersSql.toString());
                    
                    // 设置筛选参数
                    int paramIndex = 1;
                    if (periodFilter != null && !periodFilter.isEmpty()) {
                        unpaidUsersStmt.setString(paramIndex++, periodFilter);
                    }
                    if (startDateFilter != null && !startDateFilter.isEmpty()) {
                        unpaidUsersStmt.setString(paramIndex++, startDateFilter);
                    }
                    if (endDateFilter != null && !endDateFilter.isEmpty()) {
                        unpaidUsersStmt.setString(paramIndex++, endDateFilter);
                    }
                    if (buildingFilter != null && !buildingFilter.isEmpty()) {
                        unpaidUsersStmt.setString(paramIndex++, buildingFilter);
                    }
                    
                    ResultSet unpaidUsersRs = unpaidUsersStmt.executeQuery();
                    
                    List<UnpaidUser> unpaidUsersList = new ArrayList<>();
                    while (unpaidUsersRs.next()) {
                        UnpaidUser unpaidUser = new UnpaidUser();
                        unpaidUser.setUserId(unpaidUsersRs.getInt("user_id"));
                        unpaidUser.setUserName(unpaidUsersRs.getString("user_name"));
                        unpaidUser.setBuilding(unpaidUsersRs.getString("building"));
                        unpaidUser.setRoomNumber(unpaidUsersRs.getString("room_number"));
                        unpaidUser.setPeriod(unpaidUsersRs.getString("period"));
                        unpaidUser.setAmount(unpaidUsersRs.getDouble("amount"));
                        unpaidUser.setOverdueDays(unpaidUsersRs.getInt("overdue_days"));
                        unpaidUsersList.add(unpaidUser);
                    }
                    statsResponse.setUnpaidUsers(unpaidUsersList);
                    statsResponse.setTotalPages(1); // 简单实现，默认1页
                    
                    // 6. 查询趋势数据：按月份统计已缴和未缴金额
                    String trendSql = "SELECT CONCAT(YEAR(period_start), '-', LPAD(MONTH(period_start), 2, '0')) as period, " +
                                     "SUM(CASE WHEN status IN ('paid', 'partially_paid') THEN amount ELSE 0 END) as paidAmount, " +
                                     "SUM(CASE WHEN status IN ('unpaid', 'overdue') THEN amount ELSE 0 END) as unpaidAmount " +
                                     "FROM property_fee_bill " +
                                     "GROUP BY CONCAT(YEAR(period_start), '-', LPAD(MONTH(period_start), 2, '0')) " +
                                     "ORDER BY period ASC";
                    
                    PreparedStatement trendStmt = conn.prepareStatement(trendSql);
                    ResultSet trendRs = trendStmt.executeQuery();
                    
                    List<TrendData> trendDataList = new ArrayList<>();
                    while (trendRs.next()) {
                        TrendData trendData = new TrendData();
                        trendData.setPeriod(trendRs.getString("period"));
                        trendData.setPaidAmount(trendRs.getDouble("paidAmount"));
                        trendData.setUnpaidAmount(trendRs.getDouble("unpaidAmount"));
                        trendDataList.add(trendData);
                    }
                    statsResponse.setTrendData(trendDataList);
                    
                    // 关闭趋势数据查询资源
                    trendStmt.close();
                    trendRs.close();
                    
                    // 关闭未缴费用户查询资源
                    unpaidUsersStmt.close();
                    unpaidUsersRs.close();
                } finally {
                    // 关闭资源
                    if (totalHouseholdsRs != null) {
                        try { totalHouseholdsRs.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                    if (totalHouseholdsStmt != null) {
                        try { totalHouseholdsStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                    if (paidHouseholdsRs != null) {
                        try { paidHouseholdsRs.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                    if (paidHouseholdsStmt != null) {
                        try { paidHouseholdsStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                    if (conn != null) {
                        try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Statistics retrieved successfully", statsResponse)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/abnormal-payments")) {
                // 获取异常交易记录
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                List<PropertyFeePayment> abnormalPayments = paymentService.getAbnormalPayments();
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Abnormal payments retrieved successfully", abnormalPayments)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/reconcile/history")) {
                // 获取对账历史
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                List<Object> history = paymentService.getReconciliationHistory();
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Reconciliation history retrieved successfully", history)));
                return;
            }
            
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            
            // 获取当前用户信息
            String username = (String) req.getAttribute("username");
            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "Unauthorized")));
                return;
            }
            User currentUser = userService.getUserByUsername(username);
            int userId = currentUser.getId();
            
            // 读取请求体
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            
            // 居民端API
            if (relativeURI.equals("/api/property-fee/pay")) {
                // 执行支付
                PaymentRequest paymentRequest = gson.fromJson(sb.toString(), PaymentRequest.class);
                String transactionId = paymentService.executePayment(paymentRequest.getBillId(), userId, paymentRequest.getPaymentMethod(), paymentRequest.getAmount());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Payment executed successfully", transactionId)));
                return;
            } else if (relativeURI.equals("/api/property-fee/auto-deduction")) {
                // 设置自动扣费
                AutoDeductionRequest autoDeductionRequest = gson.fromJson(sb.toString(), AutoDeductionRequest.class);
                boolean result = paymentService.setAutoDeduction(userId, autoDeductionRequest.isEnabled());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Auto deduction setting updated successfully", result)));
                return;
            }
            
            // 物业管理员端API
            if (relativeURI.equals("/api/admin/property-fee/bills/batch")) {
                // 批量生成账单
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                BatchBillRequest batchBillRequest = gson.fromJson(sb.toString(), BatchBillRequest.class);
                int result = billService.batchAddBills(batchBillRequest.getBills());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Bills generated successfully", result)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/reconcile")) {
                // 自动对账
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                paymentService.autoReconcile();
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Auto reconciliation executed successfully")));
                return;
            } else if (relativeURI.startsWith("/api/admin/property-fee/reconcile/resolve/")) {
                // 解决异常交易
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                // 提取交易ID
                String[] parts = relativeURI.split("/");
                String transactionId = parts[parts.length - 1];
                boolean result = paymentService.resolveException(transactionId);
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Exception resolved successfully", result)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/generate-monthly")) {
                // 生成月度账单
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                MonthlyBillRequest monthlyBillRequest = gson.fromJson(sb.toString(), MonthlyBillRequest.class);
                billService.generateMonthlyBills(monthlyBillRequest.getYear(), monthlyBillRequest.getMonth());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Monthly bills generated successfully")));
                return;
            } else if (relativeURI.startsWith("/api/admin/property-fee/bills/") && relativeURI.endsWith("/remind")) {
                // 发送单个催缴提醒
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                // 提取账单ID
                String[] parts = relativeURI.split("/");
                int billId = Integer.parseInt(parts[parts.length - 2]);
                
                // 标记催缴提醒已发送
                int result = billService.markReminderSent(billId);
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Reminder sent successfully", result)));
                return;
            } else if (relativeURI.equals("/api/admin/property-fee/reminders")) {
                // 发送批量催缴提醒
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                // 解析请求体
                BatchReminderRequest reminderRequest = gson.fromJson(sb.toString(), BatchReminderRequest.class);
                String period = reminderRequest.getPeriod();
                Integer targetUserId = reminderRequest.getUserId();
                
                // 构建SQL查询条件
                StringBuilder sql = new StringBuilder("UPDATE property_fee_bill SET payment_deadline_reminder_sent = ? WHERE 1=1");
                List<Object> params = new ArrayList<>();
                params.add(1); // 使用1表示true，适配MySQL的TINYINT(1)类型
                
                // 添加周期过滤条件
                if (period != null && !period.isEmpty()) {
                    sql.append(" AND CONCAT(YEAR(period_start), '-', LPAD(MONTH(period_start), 2, '0')) = ?");
                    params.add(period);
                }
                
                // 添加用户ID过滤条件
                if (targetUserId != null) {
                    sql.append(" AND user_id = ?");
                    params.add(targetUserId);
                }
                
                // 执行批量更新
                Connection conn = null;
                PreparedStatement stmt = null;
                int result = 0;
                
                try {
                    conn = com.smartcommunity.util.DBUtil.getConnection();
                    stmt = conn.prepareStatement(sql.toString());
                    
                    // 设置参数
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                    
                    result = stmt.executeUpdate();
                } finally {
                    if (stmt != null) {
                        try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                    if (conn != null) {
                        try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Reminders sent successfully", result)));
                return;
            }
            
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            
            // 获取当前用户信息
            String username = (String) req.getAttribute("username");
            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "Unauthorized")));
                return;
            }
            User currentUser = userService.getUserByUsername(username);
            
            // 物业管理员端API
            if (relativeURI.startsWith("/api/admin/property-fee/bills/")) {
                // 删除账单
                if (!currentUser.getRole().equals(RoleConstants.PROPERTY_ADMIN)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                
                String[] parts = relativeURI.split("/");
                if (parts.length != 6) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid bill ID")));
                    return;
                }
                int billId = Integer.parseInt(parts[5]);
                
                int result = billService.deleteBill(billId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Bill deleted successfully", result)));
                return;
            }
            
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    // 请求类
    private static class PaymentRequest {
        private int billId;
        private String paymentMethod;
        private double amount;

        public int getBillId() {
            return billId;
        }

        public void setBillId(int billId) {
            this.billId = billId;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    private static class AutoDeductionRequest {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private static class BatchBillRequest {
        private List<PropertyFeeBill> bills;

        public List<PropertyFeeBill> getBills() {
            return bills;
        }

        public void setBills(List<PropertyFeeBill> bills) {
            this.bills = bills;
        }
    }

    private static class MonthlyBillRequest {
        private int year;
        private int month;

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }
    }

    private static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    // 批量催缴提醒请求类
    private static class BatchReminderRequest {
        private String period;
        private String type;
        private Integer userId;
        
        // Getters and Setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }

    // 响应类
    private static class Response {
        private String code;
        private String message;
        private Object data;

        public Response(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public Response(String code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    private static class StatisticsResponse {
        private int totalBills;
        private double totalPaidAmount;
        private double totalUnpaidAmount;
        private int paidCount;
        private int overdueCount;
        private int unpaidCount;
        private int partiallyPaidCount;
        private int totalPayments;
        private double totalPaymentAmount;
        private double averagePaymentAmount;
        private int totalHouseholds;
        private int paidHouseholds;
        private int unpaidHouseholds;
        private double paymentRate;
        private List<UnpaidUser> unpaidUsers;
        private List<TrendData> trendData;
        private int totalPages;

        // Getters and Setters
        public int getTotalBills() { return totalBills; }
        public void setTotalBills(int totalBills) { this.totalBills = totalBills; }
        public double getTotalPaidAmount() { return totalPaidAmount; }
        public void setTotalPaidAmount(double totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
        public double getTotalUnpaidAmount() { return totalUnpaidAmount; }
        public void setTotalUnpaidAmount(double totalUnpaidAmount) { this.totalUnpaidAmount = totalUnpaidAmount; }
        public int getPaidCount() { return paidCount; }
        public void setPaidCount(int paidCount) { this.paidCount = paidCount; }
        public int getOverdueCount() { return overdueCount; }
        public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }
        public int getUnpaidCount() { return unpaidCount; }
        public void setUnpaidCount(int unpaidCount) { this.unpaidCount = unpaidCount; }
        public int getPartiallyPaidCount() { return partiallyPaidCount; }
        public void setPartiallyPaidCount(int partiallyPaidCount) { this.partiallyPaidCount = partiallyPaidCount; }
        public int getTotalPayments() { return totalPayments; }
        public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }
        public double getTotalPaymentAmount() { return totalPaymentAmount; }
        public void setTotalPaymentAmount(double totalPaymentAmount) { this.totalPaymentAmount = totalPaymentAmount; }
        public double getAveragePaymentAmount() { return averagePaymentAmount; }
        public void setAveragePaymentAmount(double averagePaymentAmount) { this.averagePaymentAmount = averagePaymentAmount; }
        public int getTotalHouseholds() { return totalHouseholds; }
        public void setTotalHouseholds(int totalHouseholds) { this.totalHouseholds = totalHouseholds; }
        public int getPaidHouseholds() { return paidHouseholds; }
        public void setPaidHouseholds(int paidHouseholds) { this.paidHouseholds = paidHouseholds; }
        public int getUnpaidHouseholds() { return unpaidHouseholds; }
        public void setUnpaidHouseholds(int unpaidHouseholds) { this.unpaidHouseholds = unpaidHouseholds; }
        public double getPaymentRate() { return paymentRate; }
        public void setPaymentRate(double paymentRate) { this.paymentRate = paymentRate; }
        public List<UnpaidUser> getUnpaidUsers() { return unpaidUsers; }
        public void setUnpaidUsers(List<UnpaidUser> unpaidUsers) { this.unpaidUsers = unpaidUsers; }
        public List<TrendData> getTrendData() { return trendData; }
        public void setTrendData(List<TrendData> trendData) { this.trendData = trendData; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
    
    // 趋势数据类
    private static class TrendData {
        private String period;
        private double paidAmount;
        private double unpaidAmount;
        
        // Getters and Setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public double getPaidAmount() { return paidAmount; }
        public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
        public double getUnpaidAmount() { return unpaidAmount; }
        public void setUnpaidAmount(double unpaidAmount) { this.unpaidAmount = unpaidAmount; }
    }

    // 未缴费用户类
    private static class UnpaidUser {
        private int userId;
        private String userName;
        private String building;
        private String roomNumber;
        private String period;
        private double amount;
        private int overdueDays;
        
        // Getters and Setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getBuilding() { return building; }
        public void setBuilding(String building) { this.building = building; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public int getOverdueDays() { return overdueDays; }
        public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }
    }

    // 账单详情响应类
    private static class BillDetailResponse {
        private PropertyFeeBill bill;
        private User resident;

        public PropertyFeeBill getBill() {
            return bill;
        }

        public void setBill(PropertyFeeBill bill) {
            this.bill = bill;
        }

        public User getResident() {
            return resident;
        }

        public void setResident(User resident) {
            this.resident = resident;
        }
    }
}

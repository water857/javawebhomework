package com.smartcommunity.entity;

import java.sql.Timestamp;

public class VisitorRecord {
    private int id;
    private String visitorName;
    private String phone;
    private Timestamp visitTime;
    private int hostUserId;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Timestamp visitTime) {
        this.visitTime = visitTime;
    }

    public int getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(int hostUserId) {
        this.hostUserId = hostUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

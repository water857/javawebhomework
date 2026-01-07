package com.smartcommunity.entity;

import java.util.Date;
import java.util.List;

public class Repair {
    private int id;
    private int residentId;
    private String title;
    private String content;
    private String status;
    private Integer assignedTo;
    private Integer propertyAdminId;
    private Date repairTime;
    private Date completedTime;
    private Date createdAt;
    private Date updatedAt;
    private List<RepairImage> images;
    private RepairEvaluation evaluation;
    private User resident;
    private User serviceProvider;
    private User propertyAdmin;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getPropertyAdminId() {
        return propertyAdminId;
    }

    public void setPropertyAdminId(Integer propertyAdminId) {
        this.propertyAdminId = propertyAdminId;
    }

    public Date getRepairTime() {
        return repairTime;
    }

    public void setRepairTime(Date repairTime) {
        this.repairTime = repairTime;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RepairImage> getImages() {
        return images;
    }

    public void setImages(List<RepairImage> images) {
        this.images = images;
    }

    public RepairEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(RepairEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public User getResident() {
        return resident;
    }

    public void setResident(User resident) {
        this.resident = resident;
    }

    public User getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(User serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public User getPropertyAdmin() {
        return propertyAdmin;
    }

    public void setPropertyAdmin(User propertyAdmin) {
        this.propertyAdmin = propertyAdmin;
    }
}
package com.smartcommunity.dao;

import com.smartcommunity.entity.ParkingApplication;

import java.util.List;

public interface ParkingApplicationDAO {
    int createApplication(ParkingApplication application);

    ParkingApplication getApplicationById(int id);

    List<ParkingApplication> getApplicationsByUser(int userId);

    List<ParkingApplication> getAllApplications();

    int updateStatus(int id, String status);
}

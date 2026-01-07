package com.smartcommunity.dao;

import com.smartcommunity.entity.ParkingSpace;

import java.util.List;

public interface ParkingSpaceDAO {
    List<ParkingSpace> getAllSpaces();

    ParkingSpace getSpaceById(int id);

    int updateSpaceStatus(int id, String status, Integer ownerId);
}

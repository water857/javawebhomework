package com.smartcommunity.dao;

import com.smartcommunity.entity.VisitorRecord;

import java.util.List;

public interface VisitorRecordDAO {
    int createRecord(VisitorRecord record);

    List<VisitorRecord> getRecordsByHost(int hostUserId);

    List<VisitorRecord> getAllRecords();
}

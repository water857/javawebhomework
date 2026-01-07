package com.smartcommunity.dao;

import com.smartcommunity.entity.LostAndFound;

import java.util.List;

public interface LostAndFoundDAO {
    int createRecord(LostAndFound record);

    List<LostAndFound> getRecords(String type);
}

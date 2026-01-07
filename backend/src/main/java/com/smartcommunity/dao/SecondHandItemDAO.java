package com.smartcommunity.dao;

import com.smartcommunity.entity.SecondHandItem;

import java.util.List;

public interface SecondHandItemDAO {
    int createItem(SecondHandItem item);

    List<SecondHandItem> getItems(String status);

    SecondHandItem getItemById(int id);

    int updateStatus(int id, String status);
}

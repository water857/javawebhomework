package com.smartcommunity.dao;

import com.smartcommunity.entity.ActivityImage;

import java.util.List;

public interface ActivityImageDAO {
    // 上传活动图片
    int uploadImage(ActivityImage image);
    
    // 获取活动图片列表
    List<ActivityImage> getImagesByActivityId(int activityId);
    
    // 删除活动图片
    int deleteImage(int id);
    
    // 删除活动的所有图片
    int deleteImagesByActivityId(int activityId);
}
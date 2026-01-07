package com.smartcommunity.dao;

import com.smartcommunity.entity.ActivityReview;

public interface ActivityReviewDAO {
    ActivityReview getReviewByActivityId(int activityId);

    int saveReview(ActivityReview review);
}

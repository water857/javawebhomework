package com.smartcommunity.service;

import com.smartcommunity.entity.CommunityPost;
import com.smartcommunity.entity.PostComment;
import com.smartcommunity.entity.PostImage;
import com.smartcommunity.entity.Tag;
import java.util.List;

public interface CommunityPostService {
    // 发布动态
    CommunityPost publishPost(CommunityPost post, List<String> imageUrls, List<Integer> tagIds, List<Integer> visibleUserIds);
    
    // 获取动态时间线
    List<CommunityPost> getTimeline(int userId, int page, int pageSize);
    
    // 获取用户发布的动态
    List<CommunityPost> getUserPosts(int userId, int page, int pageSize);
    
    // 获取指定动态详情
    CommunityPost getPostDetail(int postId, int userId);
    
    // 点赞/取消点赞
    boolean toggleLike(int postId, int userId);
    
    // 检查用户是否点赞了动态
    boolean isLiked(int postId, int userId);
    
    // 添加评论
    PostComment addComment(int postId, int userId, String content, Integer parentCommentId);
    
    // 获取动态的评论
    List<PostComment> getPostComments(int postId);
    
    // 获取所有标签
    List<Tag> getAllTags();
    
    // 根据标签获取动态
    List<CommunityPost> getPostsByTag(int tagId, int userId, int page, int pageSize);
    
    // 搜索动态
    List<CommunityPost> searchPosts(String keyword, int userId, int page, int pageSize);
    
    // 按时间筛选动态
    List<CommunityPost> filterPostsByTime(int userId, String timeRange, int page, int pageSize);
    
    // 按热度筛选动态
    List<CommunityPost> filterPostsByPopularity(int userId, int page, int pageSize);
    
    // 更新动态隐私设置
    boolean updatePostPrivacy(int postId, int userId, String privacy);
    
    // 更新动态可见用户
    boolean updatePostVisibleUsers(int postId, int userId, List<Integer> visibleUserIds);
    
    // 获取用户的通知
    List<com.smartcommunity.entity.Notification> getUserNotifications(int userId, int page, int pageSize);
    
    // 标记通知为已读
    boolean markNotificationAsRead(int notificationId, int userId);
    
    // 上传图片
    String uploadImage(java.io.InputStream inputStream, String fileName);
}

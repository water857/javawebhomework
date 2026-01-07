package com.smartcommunity.dao;

import com.smartcommunity.entity.CommunityPost;
import com.smartcommunity.entity.PostImage;
import com.smartcommunity.entity.PostLike;
import com.smartcommunity.entity.PostComment;
import com.smartcommunity.entity.Tag;
import com.smartcommunity.entity.Notification;

import java.util.List;
import java.util.Map;

public interface CommunityPostDAO {
    // 动态相关方法
    int createPost(CommunityPost post);
    CommunityPost getPostById(int id);
    List<CommunityPost> getPosts(int page, int pageSize, Map<String, Object> filters);
    int getPostCount(Map<String, Object> filters);
    int updatePost(CommunityPost post);
    int deletePost(int id);
    int incrementViewCount(int postId);
    int updateLikeCount(int postId, int count);
    int updateCommentCount(int postId, int count);
    
    // 图片相关方法
    int addPostImage(PostImage image);
    List<PostImage> getPostImages(int postId);
    int deletePostImages(int postId);
    
    // 点赞相关方法
    int addLike(PostLike like);
    int removeLike(int postId, int userId);
    PostLike getLike(int postId, int userId);
    int getLikeCount(int postId);
    List<Integer> getLikedPostIds(int userId, List<Integer> postIds);
    
    // 评论相关方法
    int addComment(PostComment comment);
    List<PostComment> getPostComments(int postId);
    List<PostComment> getCommentsByParentId(int parentId);
    PostComment getCommentById(int id);
    int deleteComment(int id);
    int getCommentCount(int postId);
    
    // 标签相关方法
    List<Tag> getAllTags();
    Tag getTagByName(String name);
    int createTag(Tag tag);
    
    // 动态标签关联方法
    int addPostTag(int postId, int tagId);
    List<Tag> getPostTags(int postId);
    int deletePostTags(int postId);
    
    // 通知相关方法
    int createNotification(Notification notification);
    List<Notification> getUserNotifications(int userId, int page, int pageSize);
    int getUnreadNotificationCount(int userId);
    int markNotificationAsRead(int notificationId);
    int markAllNotificationsAsRead(int userId);
    
    // 隐私设置相关方法
    int addVisibleUser(int postId, int userId);
    int removeVisibleUser(int postId, int userId);
    List<Integer> getVisibleUsers(int postId);
    boolean isUserVisible(int postId, int userId);
}
package com.smartcommunity.service.impl;

import com.smartcommunity.dao.CommunityPostDAO;
import com.smartcommunity.dao.impl.CommunityPostDAOImpl;
import com.smartcommunity.entity.*;
import com.smartcommunity.service.CommunityPostService;
import com.smartcommunity.util.DBUtil;
import com.smartcommunity.util.JwtUtil;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommunityPostServiceImpl implements CommunityPostService {
    private CommunityPostDAO postDAO = new CommunityPostDAOImpl();

    @Override
    public CommunityPost publishPost(CommunityPost post, List<String> imageUrls, List<Integer> tagIds, List<Integer> visibleUserIds) {
        try {
            // 1. 插入动态
            int postId = postDAO.createPost(post);
            
            // 2. 插入图片
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    PostImage postImage = new PostImage();
                    postImage.setPostId(postId);
                    postImage.setImageUrl(imageUrl);
                    postDAO.addPostImage(postImage);
                }
            }
            
            // 3. 插入标签关联
            if (tagIds != null && !tagIds.isEmpty()) {
                for (Integer tagId : tagIds) {
                    postDAO.addPostTag(postId, tagId);
                }
            }
            
            // 4. 设置可见用户（如果是指定用户可见）
            if ("specified_users".equals(post.getPrivacy()) && visibleUserIds != null && !visibleUserIds.isEmpty()) {
                for (Integer userId : visibleUserIds) {
                    postDAO.addVisibleUser(postId, userId);
                    
                    // 发送通知给指定用户
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setType("post");
                    notification.setRelatedId(postId);
                    notification.setContent("有人发布了一条仅你可见的动态");
                    postDAO.createNotification(notification);
                }
            }
            
            // 5. 返回完整的动态信息
            return postDAO.getPostById(postId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("发布动态失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> getTimeline(int userId, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("visibleUserId", userId);
            filters.put("currentUserId", userId); // 添加当前用户ID用于加载点赞状态
            filters.put("orderBy", "createdAt desc");
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取时间线失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> getUserPosts(int userId, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("userId", userId);
            filters.put("orderBy", "createdAt desc");
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取用户动态失败: " + e.getMessage());
        }
    }

    @Override
    public CommunityPost getPostDetail(int postId, int userId) {
        try {
            CommunityPost post = postDAO.getPostById(postId);
            if (post != null) {
                // 增加浏览量
                postDAO.incrementViewCount(postId);
            }
            return post;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取动态详情失败: " + e.getMessage());
        }
    }

    @Override
    public boolean toggleLike(int postId, int userId) {
        try {
            if (postDAO.getLike(postId, userId) != null) {
                // 取消点赞
                boolean success = postDAO.removeLike(postId, userId) > 0;
                if (success) {
                    // 减少点赞数，确保不小于0
                    postDAO.updateLikeCount(postId, -1);
                }
                return success;
            } else {
                // 添加点赞
                PostLike postLike = new PostLike();
                postLike.setPostId(postId);
                postLike.setUserId(userId);
                boolean success = postDAO.addLike(postLike) > 0;
                if (success) {
                    // 增加点赞数
                    postDAO.updateLikeCount(postId, 1);
                    
                    // 发送点赞通知
                    CommunityPost post = postDAO.getPostById(postId);
                    if (post != null && post.getUserId() != userId) {
                        Notification notification = new Notification();
                        notification.setUserId(post.getUserId());
                        notification.setType("like");
                        notification.setRelatedId(postId);
                        notification.setContent("有人点赞了你的动态");
                        postDAO.createNotification(notification);
                    }
                }
                return success;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("点赞操作失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isLiked(int postId, int userId) {
        try {
            return postDAO.getLike(postId, userId) != null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("检查点赞状态失败: " + e.getMessage());
        }
    }

    @Override
    public PostComment addComment(int postId, int userId, String content, Integer parentCommentId) {
        try {
            PostComment comment = new PostComment();
            comment.setPostId(postId);
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setParentId(parentCommentId);
            
            int commentId = postDAO.addComment(comment);
            if (commentId == -1) {
                throw new RuntimeException("添加评论失败: 数据库操作失败");
            }
            comment.setId(commentId);
            
            // 发送评论通知
            CommunityPost post = postDAO.getPostById(postId);
            if (post != null && post.getUserId() != userId) {
                Notification notification = new Notification();
                notification.setUserId(post.getUserId());
                notification.setType("comment");
                notification.setRelatedId(postId);
                notification.setContent("有人评论了你的动态");
                postDAO.createNotification(notification);
            }
            
            return comment;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("添加评论失败: " + e.getMessage());
        }
    }

    @Override
    public List<PostComment> getPostComments(int postId) {
        try {
            return postDAO.getPostComments(postId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取评论失败: " + e.getMessage());
        }
    }

    @Override
    public List<Tag> getAllTags() {
        try {
            return postDAO.getAllTags();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取标签失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> getPostsByTag(int tagId, int userId, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("tagId", tagId);
            filters.put("visibleUserId", userId);
            filters.put("page", page);
            filters.put("pageSize", pageSize);
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取标签动态失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> searchPosts(String keyword, int userId, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("keyword", keyword);
            filters.put("visibleUserId", userId);
            filters.put("page", page);
            filters.put("pageSize", pageSize);
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("搜索动态失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> filterPostsByTime(int userId, String timeRange, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("timeRange", timeRange);
            filters.put("visibleUserId", userId);
            filters.put("page", page);
            filters.put("pageSize", pageSize);
            filters.put("orderBy", "createdAt desc");
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("按时间筛选动态失败: " + e.getMessage());
        }
    }

    @Override
    public List<CommunityPost> filterPostsByPopularity(int userId, int page, int pageSize) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("visibleUserId", userId);
            filters.put("page", page);
            filters.put("pageSize", pageSize);
            filters.put("orderBy", "likeCount desc, commentCount desc, createdAt desc");
            return postDAO.getPosts(page, pageSize, filters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("按热度筛选动态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updatePostPrivacy(int postId, int userId, String privacy) {
        try {
            CommunityPost post = postDAO.getPostById(postId);
            if (post != null && post.getUserId() == userId) {
                post.setPrivacy(privacy);
                return postDAO.updatePost(post) > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新动态隐私设置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updatePostVisibleUsers(int postId, int userId, List<Integer> visibleUserIds) {
        try {
            // 先删除旧的可见用户设置
            List<Integer> oldVisibleUsers = postDAO.getVisibleUsers(postId);
            if (oldVisibleUsers != null && !oldVisibleUsers.isEmpty()) {
                for (Integer oldUserId : oldVisibleUsers) {
                    postDAO.removeVisibleUser(postId, oldUserId);
                }
            }
            
            // 添加新的可见用户
            if (visibleUserIds != null && !visibleUserIds.isEmpty()) {
                for (Integer visibleUserId : visibleUserIds) {
                    postDAO.addVisibleUser(postId, visibleUserId);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新动态可见用户失败: " + e.getMessage());
        }
    }

    @Override
    public List<com.smartcommunity.entity.Notification> getUserNotifications(int userId, int page, int pageSize) {
        try {
            return postDAO.getUserNotifications(userId, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取通知失败: " + e.getMessage());
        }
    }

    @Override
    public boolean markNotificationAsRead(int notificationId, int userId) {
        try {
            return postDAO.markNotificationAsRead(notificationId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("标记通知已读失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadImage(InputStream inputStream, String fileName) {
        try {
            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            
            // 保存文件到uploads目录
            String uploadDir = System.getProperty("user.dir") + "/backend/src/main/webapp/uploads";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // 返回图片URL
            return "/uploads/" + uniqueFileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("上传图片失败: " + e.getMessage());
        }
    }
}

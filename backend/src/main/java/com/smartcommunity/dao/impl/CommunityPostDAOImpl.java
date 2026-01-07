package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.CommunityPostDAO;
import com.smartcommunity.entity.*;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityPostDAOImpl implements CommunityPostDAO {
    // 动态相关方法
    @Override
    public int createPost(CommunityPost post) {
        String sql = "INSERT INTO community_posts (user_id, content, privacy, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, post.getUserId());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getPrivacy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    @Override
    public CommunityPost getPostById(int id) {
        String sql = "SELECT * FROM community_posts WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CommunityPost post = mapPostResultSet(rs);
                    
                    // 加载关联数据
                    List<Integer> postIds = new ArrayList<>();
                    postIds.add(post.getId());
                    
                    // 加载用户信息
                    Map<Integer, User> userMap = getUserMap(postIds);
                    post.setUser(userMap.get(post.getUserId()));
                    
                    // 加载图片
                    Map<Integer, List<PostImage>> imageMap = getPostImageMap(postIds);
                    post.setImages(imageMap.getOrDefault(post.getId(), new ArrayList<>()));
                    
                    // 加载标签
                    Map<Integer, List<Tag>> tagMap = getPostTagMap(postIds);
                    post.setTags(tagMap.getOrDefault(post.getId(), new ArrayList<>()));
                    
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public List<CommunityPost> getPosts(int page, int pageSize, Map<String, Object> filters) {
        // 简化查询，直接返回所有公开帖子，不考虑复杂的过滤条件
        String sql = "SELECT * FROM community_posts WHERE privacy = 'public' ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("数据库连接成功: " + conn);
            
            System.out.println("执行的SQL语句: " + sql);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 设置分页参数
                int paramIndex = 1;
                pstmt.setInt(paramIndex++, pageSize);
                pstmt.setInt(paramIndex, (page - 1) * pageSize);
                
                System.out.println("pageSize参数: " + pageSize);
                System.out.println("offset参数: " + (page - 1) * pageSize);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("查询执行成功，开始处理结果集");
                    List<CommunityPost> posts = new ArrayList<>();
                    while (rs.next()) {
                        CommunityPost post = mapPostResultSet(rs);
                        System.out.println("查询到的帖子: " + post.getId() + " - " + post.getContent());
                        posts.add(post);
                    }
                    
                    System.out.println("查询到的帖子数量: " + posts.size());
                    
                    // 加载关联数据
                    if (!posts.isEmpty()) {
                        List<Integer> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toList());
                        
                        System.out.println("帖子ID列表: " + postIds);
                        
                        // 加载用户信息
                        Map<Integer, User> userMap = getUserMap(postIds);
                        System.out.println("获取到的用户信息: " + userMap);
                        posts.forEach(post -> post.setUser(userMap.get(post.getUserId())));
                        
                        // 加载图片
                        Map<Integer, List<PostImage>> imageMap = getPostImageMap(postIds);
                        posts.forEach(post -> post.setImages(imageMap.getOrDefault(post.getId(), new ArrayList<>())));
                        
                        // 加载标签
                        Map<Integer, List<Tag>> tagMap = getPostTagMap(postIds);
                        posts.forEach(post -> post.setTags(tagMap.getOrDefault(post.getId(), new ArrayList<>())));
                        
                        // 加载点赞状态
                        if (filters.containsKey("currentUserId")) {
                            List<Integer> likedPostIds = getLikedPostIds((int) filters.get("currentUserId"), postIds);
                            posts.forEach(post -> post.setLiked(likedPostIds.contains(post.getId())));
                        }
                    }
                    
                    return posts;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL异常: " + e.getMessage());
            System.out.println("SQL状态: " + e.getSQLState());
            System.out.println("错误代码: " + e.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("其他异常: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    @Override
    public int getPostCount(Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM community_posts WHERE 1=1");
        
        // 添加过滤条件
        if (filters.containsKey("userId")) {
            sql.append(" AND user_id = ?");
        }
        if (filters.containsKey("privacy")) {
            sql.append(" AND privacy = ?");
        }
        if (filters.containsKey("tagId")) {
            sql.append(" AND id IN (SELECT post_id FROM post_tags WHERE tag_id = ?)");
        }
        if (filters.containsKey("searchKeyword")) {
            sql.append(" AND content LIKE ?");
        }
        if (filters.containsKey("keyword")) {
            sql.append(" AND content LIKE ?");
        }
        if (filters.containsKey("visibleUserId")) {
            sql.append(" AND (privacy = 'public' OR id IN (SELECT post_id FROM post_visible_users WHERE user_id = ?))");
        }
        if (filters.containsKey("timeRange")) {
            // 根据时间范围过滤
            String timeRange = (String) filters.get("timeRange");
            if ("latest".equals(timeRange)) {
                // 最近的帖子，按创建时间倒序即可
            } else if ("week".equals(timeRange)) {
                sql.append(" AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)");
            } else if ("month".equals(timeRange)) {
                sql.append(" AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)");
            }
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            
            // 设置参数
            if (filters.containsKey("userId")) {
                pstmt.setInt(paramIndex++, (int) filters.get("userId"));
            }
            if (filters.containsKey("privacy")) {
                pstmt.setString(paramIndex++, (String) filters.get("privacy"));
            }
            if (filters.containsKey("tagId")) {
                pstmt.setInt(paramIndex++, (int) filters.get("tagId"));
            }
            if (filters.containsKey("searchKeyword")) {
                pstmt.setString(paramIndex++, "%" + filters.get("searchKeyword") + "%");
            }
            if (filters.containsKey("keyword")) {
                pstmt.setString(paramIndex++, "%" + filters.get("keyword") + "%");
            }
            if (filters.containsKey("visibleUserId")) {
                pstmt.setInt(paramIndex++, (int) filters.get("visibleUserId"));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int updatePost(CommunityPost post) {
        String sql = "UPDATE community_posts SET content = ?, privacy = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, post.getContent());
            pstmt.setString(2, post.getPrivacy());
            pstmt.setInt(3, post.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int deletePost(int id) {
        String sql = "DELETE FROM community_posts WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int incrementViewCount(int postId) {
        String sql = "UPDATE community_posts SET view_count = view_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int updateLikeCount(int postId, int count) {
        // 使用GREATEST函数确保点赞数不小于0
        String sql = "UPDATE community_posts SET like_count = GREATEST(like_count + ?, 0) WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setInt(2, postId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int updateCommentCount(int postId, int count) {
        // 使用GREATEST函数确保评论数不小于0
        String sql = "UPDATE community_posts SET comment_count = GREATEST(comment_count + ?, 0) WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, count);
            pstmt.setInt(2, postId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 图片相关方法
    @Override
    public int addPostImage(PostImage image) {
        String sql = "INSERT INTO post_images (post_id, image_url, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, image.getPostId());
            pstmt.setString(2, image.getImageUrl());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    @Override
    public List<PostImage> getPostImages(int postId) {
        String sql = "SELECT * FROM post_images WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<PostImage> images = new ArrayList<>();
                while (rs.next()) {
                    images.add(mapImageResultSet(rs));
                }
                return images;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public int deletePostImages(int postId) {
        String sql = "DELETE FROM post_images WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 点赞相关方法
    @Override
    public int addLike(PostLike like) {
        String sql = "INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, like.getPostId());
            pstmt.setInt(2, like.getUserId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int removeLike(int postId, int userId) {
        String sql = "DELETE FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public PostLike getLike(int postId, int userId) {
        String sql = "SELECT * FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapLikeResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int getLikeCount(int postId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public List<Integer> getLikedPostIds(int userId, List<Integer> postIds) {
        if (postIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT post_id FROM post_likes WHERE user_id = ? AND post_id IN (");
        for (int i = 0; i < postIds.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            
            for (int postId : postIds) {
                pstmt.setInt(paramIndex++, postId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Integer> likedIds = new ArrayList<>();
                while (rs.next()) {
                    likedIds.add(rs.getInt("post_id"));
                }
                return likedIds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    // 评论相关方法
    @Override
    public int addComment(PostComment comment) {
        String sql = "INSERT INTO post_comments (post_id, user_id, content, parent_id, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, comment.getPostId());
            pstmt.setInt(2, comment.getUserId());
            pstmt.setString(3, comment.getContent());
            if (comment.getParentId() != null) {
                pstmt.setInt(4, comment.getParentId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    @Override
    public List<PostComment> getPostComments(int postId) {
        String sql = "SELECT * FROM post_comments WHERE post_id = ? AND parent_id IS NULL ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<PostComment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(mapCommentResultSet(rs));
                }
                
                // 加载用户信息和回复
                if (!comments.isEmpty()) {
                    List<Integer> commentIds = comments.stream().map(PostComment::getId).collect(Collectors.toList());
                    
                    // 加载用户信息
                    Map<Integer, User> userMap = getCommentUserMap(postId);
                    comments.forEach(comment -> comment.setUser(userMap.get(comment.getUserId())));
                    
                    // 加载回复
                    Map<Integer, List<PostComment>> replyMap = new HashMap<>();
                    List<PostComment> allReplies = getCommentsByParentIds(commentIds);
                    allReplies.forEach(reply -> {
                        reply.setUser(userMap.get(reply.getUserId()));
                        replyMap.computeIfAbsent(reply.getParentId(), k -> new ArrayList<>()).add(reply);
                    });
                    
                    comments.forEach(comment -> comment.setReplies(replyMap.getOrDefault(comment.getId(), new ArrayList<>())));
                }
                
                return comments;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<PostComment> getCommentsByParentId(int parentId) {
        String sql = "SELECT * FROM post_comments WHERE parent_id = ? ORDER BY created_at ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<PostComment> replies = new ArrayList<>();
                while (rs.next()) {
                    replies.add(mapCommentResultSet(rs));
                }
                return replies;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public PostComment getCommentById(int id) {
        String sql = "SELECT * FROM post_comments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCommentResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int deleteComment(int id) {
        String sql = "DELETE FROM post_comments WHERE id = ? OR parent_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int getCommentCount(int postId) {
        String sql = "SELECT COUNT(*) FROM post_comments WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 标签相关方法
    @Override
    public List<Tag> getAllTags() {
        String sql = "SELECT * FROM tags ORDER BY name";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Tag> tags = new ArrayList<>();
            while (rs.next()) {
                tags.add(mapTagResultSet(rs));
            }
            return tags;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public Tag getTagByName(String name) {
        String sql = "SELECT * FROM tags WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapTagResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public int createTag(Tag tag) {
        String sql = "INSERT INTO tags (name, created_at) VALUES (?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tag.getName());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // 动态标签关联方法
    @Override
    public int addPostTag(int postId, int tagId) {
        String sql = "INSERT INTO post_tags (post_id, tag_id, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, tagId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public List<Tag> getPostTags(int postId) {
        String sql = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Tag> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(mapTagResultSet(rs));
                }
                return tags;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public int deletePostTags(int postId) {
        String sql = "DELETE FROM post_tags WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 通知相关方法
    @Override
    public int createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, related_id, content, is_read, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getType());
            if (notification.getRelatedId() != null) {
                pstmt.setInt(3, notification.getRelatedId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setString(4, notification.getContent());
            pstmt.setBoolean(5, notification.isRead());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public List<Notification> getUserNotifications(int userId, int page, int pageSize) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (rs.next()) {
                    notifications.add(mapNotificationResultSet(rs));
                }
                return notifications;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public int getUnreadNotificationCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int markNotificationAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int markAllNotificationsAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // 隐私设置相关方法
    @Override
    public int addVisibleUser(int postId, int userId) {
        String sql = "INSERT INTO post_visible_users (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public int removeVisibleUser(int postId, int userId) {
        String sql = "DELETE FROM post_visible_users WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public List<Integer> getVisibleUsers(int postId) {
        String sql = "SELECT user_id FROM post_visible_users WHERE post_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Integer> userIds = new ArrayList<>();
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
                return userIds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    @Override
    public boolean isUserVisible(int postId, int userId) {
        String sql = "SELECT COUNT(*) FROM post_visible_users WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 辅助方法
    private CommunityPost mapPostResultSet(ResultSet rs) throws SQLException {
        CommunityPost post = new CommunityPost();
        post.setId(rs.getInt("id"));
        post.setUserId(rs.getInt("user_id"));
        post.setContent(rs.getString("content"));
        post.setPrivacy(rs.getString("privacy"));
        post.setLikeCount(rs.getInt("like_count"));
        post.setCommentCount(rs.getInt("comment_count"));
        post.setViewCount(rs.getInt("view_count"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setUpdatedAt(rs.getTimestamp("updated_at"));
        return post;
    }
    
    private PostImage mapImageResultSet(ResultSet rs) throws SQLException {
        PostImage image = new PostImage();
        image.setId(rs.getInt("id"));
        image.setPostId(rs.getInt("post_id"));
        image.setImageUrl(rs.getString("image_url"));
        image.setCreatedAt(rs.getTimestamp("created_at"));
        return image;
    }
    
    private PostLike mapLikeResultSet(ResultSet rs) throws SQLException {
        PostLike like = new PostLike();
        like.setId(rs.getInt("id"));
        like.setPostId(rs.getInt("post_id"));
        like.setUserId(rs.getInt("user_id"));
        like.setCreatedAt(rs.getTimestamp("created_at"));
        return like;
    }
    
    private PostComment mapCommentResultSet(ResultSet rs) throws SQLException {
        PostComment comment = new PostComment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setContent(rs.getString("content"));
        // 修复：正确处理NULL值的parent_id
        Object parentIdObj = rs.getObject("parent_id");
        comment.setParentId(parentIdObj != null ? (Integer) parentIdObj : null);
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        return comment;
    }
    
    private Tag mapTagResultSet(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getInt("id"));
        tag.setName(rs.getString("name"));
        tag.setCreatedAt(rs.getTimestamp("created_at"));
        return tag;
    }
    
    private Notification mapNotificationResultSet(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setType(rs.getString("type"));
        notification.setRelatedId(rs.getInt("related_id"));
        notification.setContent(rs.getString("content"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at"));
        return notification;
    }
    
    private Map<Integer, User> getUserMap(List<Integer> postIds) throws SQLException {
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT DISTINCT u.* FROM user u JOIN community_posts p ON u.id = p.user_id WHERE p.id IN (");
        for (int i = 0; i < postIds.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (int postId : postIds) {
                pstmt.setInt(paramIndex++, postId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<Integer, User> userMap = new HashMap<>();
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRealName(rs.getString("real_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setAddress(rs.getString("address"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getInt("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    userMap.put(user.getId(), user);
                }
                return userMap;
            }
        }
    }
    
    private Map<Integer, List<PostImage>> getPostImageMap(List<Integer> postIds) throws SQLException {
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM post_images WHERE post_id IN (");
        for (int i = 0; i < postIds.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (int postId : postIds) {
                pstmt.setInt(paramIndex++, postId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<Integer, List<PostImage>> imageMap = new HashMap<>();
                while (rs.next()) {
                    PostImage image = mapImageResultSet(rs);
                    imageMap.computeIfAbsent(image.getPostId(), k -> new ArrayList<>()).add(image);
                }
                return imageMap;
            }
        }
    }
    
    private Map<Integer, List<Tag>> getPostTagMap(List<Integer> postIds) throws SQLException {
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT t.*, pt.post_id FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id IN (");
        for (int i = 0; i < postIds.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (int postId : postIds) {
                pstmt.setInt(paramIndex++, postId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<Integer, List<Tag>> tagMap = new HashMap<>();
                while (rs.next()) {
                    Tag tag = mapTagResultSet(rs);
                    int postId = rs.getInt("post_id");
                    tagMap.computeIfAbsent(postId, k -> new ArrayList<>()).add(tag);
                }
                return tagMap;
            }
        }
    }
    
    private Map<Integer, User> getCommentUserMap(int postId) {
        String sql = "SELECT DISTINCT u.* FROM user u JOIN post_comments c ON u.id = c.user_id WHERE c.post_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<Integer, User> userMap = new HashMap<>();
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRealName(rs.getString("real_name"));
                    user.setPhone(rs.getString("phone"));
                    user.setEmail(rs.getString("email"));
                    user.setAddress(rs.getString("address"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getInt("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    userMap.put(user.getId(), user);
                }
                return userMap;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    private List<PostComment> getCommentsByParentIds(List<Integer> parentIds) {
        if (parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM post_comments WHERE parent_id IN (");
        for (int i = 0; i < parentIds.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(") ORDER BY created_at ASC");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (int parentId : parentIds) {
                pstmt.setInt(paramIndex++, parentId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<PostComment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(mapCommentResultSet(rs));
                }
                return comments;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
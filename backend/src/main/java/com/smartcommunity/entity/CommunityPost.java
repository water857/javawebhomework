package com.smartcommunity.entity;

import java.util.Date;
import java.util.List;

public class CommunityPost {
    private int id;
    private int userId;
    private String content;
    private String privacy;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private Date createdAt;
    private Date updatedAt;
    
    // 关联属性
    private User user;
    private List<PostImage> images;
    private List<PostComment> comments;
    private List<Tag> tags;
    private boolean isLiked;
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getPrivacy() {
        return privacy;
    }
    
    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public int getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<PostImage> getImages() {
        return images;
    }
    
    public void setImages(List<PostImage> images) {
        this.images = images;
    }
    
    public List<PostComment> getComments() {
        return comments;
    }
    
    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }
    
    public List<Tag> getTags() {
        return tags;
    }
    
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
    
    public boolean isLiked() {
        return isLiked;
    }
    
    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
package com.smartcommunity.entity;

import java.util.Date;
import java.util.List;

public class PostComment {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private Integer parentId;
    private Date createdAt;
    
    // 关联属性
    private User user;
    private PostComment parentComment;
    private List<PostComment> replies;
    
    // 访问器与设置器方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getPostId() {
        return postId;
    }
    
    public void setPostId(int postId) {
        this.postId = postId;
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
    
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public PostComment getParentComment() {
        return parentComment;
    }
    
    public void setParentComment(PostComment parentComment) {
        this.parentComment = parentComment;
    }
    
    public List<PostComment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<PostComment> replies) {
        this.replies = replies;
    }
}
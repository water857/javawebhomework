package com.smartcommunity.websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/{userId}")
public class MessageWebSocket {
    
    private static ConcurrentHashMap<String, MessageWebSocket> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String userId;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        webSocketMap.put(userId, this);
        System.out.println("WebSocket连接已建立，用户ID: " + userId + ", 当前连接数: " + webSocketMap.size());
    }
    
    @OnClose
    public void onClose() {
        webSocketMap.remove(userId);
        System.out.println("WebSocket连接已关闭，用户ID: " + userId + ", 当前连接数: " + webSocketMap.size());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到客户端消息，用户ID: " + userId + ", 消息: " + message);
        // 可以在这里处理客户端发送的消息
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket发生错误，用户ID: " + userId);
        error.printStackTrace();
    }
    
    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(String userId, String message) {
        MessageWebSocket webSocket = webSocketMap.get(userId);
        if (webSocket != null && webSocket.session.isOpen()) {
            try {
                webSocket.session.getBasicRemote().sendText(message);
                System.out.println("已发送消息给用户ID: " + userId + ", 消息: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 发送消息给所有用户
     */
    public static void sendMessageToAll(String message) {
        for (MessageWebSocket webSocket : webSocketMap.values()) {
            if (webSocket.session.isOpen()) {
                try {
                    webSocket.session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("已发送消息给所有用户，消息: " + message);
    }
    
    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return webSocketMap.size();
    }
}
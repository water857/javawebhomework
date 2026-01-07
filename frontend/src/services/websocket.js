// WebSocket服务

class WebSocketService {
    constructor() {
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
        this.listeners = new Map();
        this.userId = null;
    }

    // 连接WebSocket服务器
    connect(userId) {
        if (this.socket && this.isConnected) {
            console.log('WebSocket已经连接');
            return;
        }

        this.userId = userId;
        const wsUrl = `ws://localhost:8080/backend/websocket/${userId}`;
        
        try {
            this.socket = new WebSocket(wsUrl);
            
            this.socket.onopen = () => {
                console.log('WebSocket连接成功');
                this.isConnected = true;
                this.reconnectAttempts = 0;
            };
            
            this.socket.onmessage = (event) => {
                console.log('收到WebSocket消息:', event.data);
                this.handleMessage(event.data);
            };
            
            this.socket.onclose = () => {
                console.log('WebSocket连接关闭');
                this.isConnected = false;
                this.attemptReconnect();
            };
            
            this.socket.onerror = (error) => {
                console.error('WebSocket错误:', error);
                this.isConnected = false;
            };
        } catch (error) {
            console.error('WebSocket连接失败:', error);
            this.isConnected = false;
            this.attemptReconnect();
        }
    }

    // 重新连接
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect(this.userId);
            }, this.reconnectDelay);
        } else {
            console.error('WebSocket重连失败，已达到最大重试次数');
        }
    }

    // 发送消息
    sendMessage(message) {
        if (this.socket && this.isConnected) {
            try {
                this.socket.send(JSON.stringify(message));
                console.log('发送WebSocket消息:', message);
            } catch (error) {
                console.error('发送WebSocket消息失败:', error);
            }
        } else {
            console.error('WebSocket未连接，无法发送消息');
        }
    }

    // 处理接收到的消息
    handleMessage(messageData) {
        try {
            const message = JSON.parse(messageData);
            const messageType = message.type;
            
            // 调用对应的监听器
            if (this.listeners.has(messageType)) {
                const typeListeners = this.listeners.get(messageType);
                typeListeners.forEach(listener => {
                    try {
                        listener(message);
                    } catch (error) {
                        console.error('处理WebSocket消息监听器失败:', error);
                    }
                });
            }
            
            // 调用全局监听器
            if (this.listeners.has('*')) {
                const globalListeners = this.listeners.get('*');
                globalListeners.forEach(listener => {
                    try {
                        listener(message);
                    } catch (error) {
                        console.error('处理WebSocket全局监听器失败:', error);
                    }
                });
            }
        } catch (error) {
            console.error('解析WebSocket消息失败:', error);
        }
    }

    // 订阅消息类型
    subscribe(messageType, listener) {
        if (!this.listeners.has(messageType)) {
            this.listeners.set(messageType, []);
        }
        this.listeners.get(messageType).push(listener);
        
        return () => {
            this.unsubscribe(messageType, listener);
        };
    }

    // 取消订阅
    unsubscribe(messageType, listener) {
        if (this.listeners.has(messageType)) {
            const typeListeners = this.listeners.get(messageType);
            const index = typeListeners.indexOf(listener);
            if (index > -1) {
                typeListeners.splice(index, 1);
            }
        }
    }

    // 断开连接
    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
            this.isConnected = false;
            this.listeners.clear();
            console.log('WebSocket连接已断开');
        }
    }

    // 获取连接状态
    getConnectionStatus() {
        return this.isConnected;
    }
}

// 导出单例实例
export default new WebSocketService();
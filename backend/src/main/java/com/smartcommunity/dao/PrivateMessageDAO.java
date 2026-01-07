package com.smartcommunity.dao;

import com.smartcommunity.entity.PrivateConversation;
import com.smartcommunity.entity.PrivateMessage;

import java.util.List;

public interface PrivateMessageDAO {
    int sendMessage(PrivateMessage message);

    List<PrivateMessage> getConversation(int userId, int otherUserId);

    List<PrivateConversation> getConversationList(int userId);

    int markReadByMessageId(int messageId, int userId);

    int markReadByUser(int userId, int otherUserId);
}

package com.dql.websocket.model;

import lombok.Data;

@Data
public class ChatMessage {

    private MessageType type;

    private String content;

    private String sender;

    private Long userNum;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}

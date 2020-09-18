package com.dql.websocket.controller;

import com.dql.websocket.model.ChatMessage;
import com.dql.websocket.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WebSocketController {

    @Value("${redis.channel.msgToAll}")
    private String msgToAll;

    @Value("${redis.set.onlineUsers}")
    private String onlineUsers;

    @Value("${redis.channel.userStatus}")
    private String userStatus;

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public WebSocketController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        try {
            redisTemplate.convertAndSend(msgToAll, JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

        log.info("User added in Chatroom:" + chatMessage.getSender());
        try {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            redisTemplate.opsForSet().add(onlineUsers, chatMessage.getSender());
            redisTemplate.convertAndSend(userStatus, JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

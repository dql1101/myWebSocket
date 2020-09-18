package com.dql.websocket.listener;

import com.dql.websocket.model.ChatMessage;
import com.dql.websocket.service.WebSocketService;
import com.dql.websocket.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Redis订阅频道处理类
 */
@Component
@Slf4j
public class RedisListenerHandler extends MessageListenerAdapter {

    @Value("${redis.channel.msgToAll}")
    private String msgToAll;

    @Value("${redis.channel.userStatus}")
    private String userStatus;

    private final RedisTemplate<String, String> redisTemplate;

    private final WebSocketService webSocketService;

    @Autowired
    public RedisListenerHandler(RedisTemplate<String, String> redisTemplate, WebSocketService webSocketService) {
        this.redisTemplate = redisTemplate;
        this.webSocketService = webSocketService;
    }

    /**
     * 收到监听消息
     * @param message
     * @param bytes
     */
    @Override
    public void onMessage(Message message, byte[] bytes) {
        byte[] body = message.getBody();
        byte[] channel = message.getChannel();
        String rawMsg;
        String topic;
        try {
            rawMsg = redisTemplate.getStringSerializer().deserialize(body);
            topic = redisTemplate.getStringSerializer().deserialize(channel);
            log.info("Received raw message from topic:" + topic + ", raw message content：" + rawMsg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        if (msgToAll.equals(topic)) {
            log.info("Send message to all users:" + rawMsg);
            ChatMessage chatMessage = JsonUtil.parseJsonToObj(rawMsg, ChatMessage.class);
            // 发送消息给所有在线Cid
            webSocketService.sendMsg(chatMessage);
        } else if (userStatus.equals(topic)) {
            ChatMessage chatMessage = JsonUtil.parseJsonToObj(rawMsg, ChatMessage.class);
            if (chatMessage != null) {
                webSocketService.alertUserStatus(chatMessage);
            }
        } else {
            log.warn("No further operation with this topic!");
        }
    }

}

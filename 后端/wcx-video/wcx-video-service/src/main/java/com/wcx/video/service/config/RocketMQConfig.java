package com.wcx.video.service.config;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wcx.video.domain.UserFollowing;
import com.wcx.video.domain.UserMoment;
import com.wcx.video.domain.constant.MQConstant;
import com.wcx.video.service.UserFollowingService;
import com.wcx.video.service.websocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserFollowingService userFollowingService;

    //用户动态 生产者
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws MQClientException {
        //设置生产者的属性
        DefaultMQProducer producer = new DefaultMQProducer(MQConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddr);
        //启动生产者
        producer.start();
        return producer;
    }

    //用户动态 消费者
    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
        //设置消费者的属性
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MQConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        //消费者订阅TOPIC_MOMENTS的主题和所有子主题
        consumer.subscribe(MQConstant.TOPIC_MOMENTS, "*");
        //添加监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            //MessageExt类继承Message类，扩展了Message类
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //取出消息
                MessageExt msg = msgs.get(0);
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                //获去发布动态的userId
                Long userId = userMoment.getUserId();
                //获取发布动态userId
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                //uo主把自己发布的动态存入自己每个粉丝的动态列表中
                for (UserFollowing fan : fanList) {
                    //key为subscribed-#{userId},值为userId关注的所有up主发布的动态，以一个链表的形式存储
                    String key = "subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    if (StringUtil.isNullOrEmpty(subscribedListStr)) {
                        subscribedList = new ArrayList<>();
                    } else {
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //启动消费者
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws MQClientException {
        //实例化一个GROUP_DANMUS下的生产者
        DefaultMQProducer producer = new DefaultMQProducer(MQConstant.GROUP_DANMUS);
        //设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        //启动生产者实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        //实例化一个GROUP_DANMUS下的消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(MQConstant.GROUP_DANMUS);
        consumer.setNamesrvAddr(nameServerAddr);
        //定义一个或者多个主题(Topic)
        consumer.subscribe(MQConstant.TOPIC_DANMUS, "*");
        //注册回调实现类处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //从消息体中提取中sessionId和弹幕消息，根据sessionId找到对应的会话，把弹幕消息发送出去
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if (webSocketService.getSession().isOpen()) {
                    try {
                        //向前端主动推送弹幕消息
                        webSocketService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //启动消费者实例
        consumer.start();
        return consumer;
    }
}

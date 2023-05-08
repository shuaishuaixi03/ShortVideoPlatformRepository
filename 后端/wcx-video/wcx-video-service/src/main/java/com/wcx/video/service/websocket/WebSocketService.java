package com.wcx.video.service.websocket;

import com.alibaba.fastjson.JSONObject;
import com.wcx.video.domain.Danmu;
import com.wcx.video.domain.constant.MQConstant;
import com.wcx.video.service.DanmuService;
import com.wcx.video.service.config.WebSocketConfig;
import com.wcx.video.service.utils.RocketMQUtil;
import com.wcx.video.service.utils.TokenUtil;
import io.netty.util.internal.StringUtil;
import javafx.application.Application;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
//用来标识这个服务类和WebSocket相关
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {

    //用来记录日志
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //用来记录WebSocket的连接数，高并发下使用原子类，防止数据错误
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    //WebSocket中的会话
    private Session session;

    //会话Id
    private String sessionId;

    private Long userId;


    private static ApplicationContext APPLICATION_CONTEXT;

    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        try{
            this.userId = TokenUtil.verifyToken(token);
        }catch (Exception e){}
        this.sessionId = session.getId();
        this.session = session;
        //当前会话是否在WebSocket的连接中
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            //先从WebSocket移除当前会话后加入当前会话
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            //将当前会话连接加入到WebSocket中
            WEBSOCKET_MAP.put(sessionId, this);
            //WebSocket连接人数加1
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功:" + sessionId + "，当前在线人数为: " + ONLINE_COUNT);
        try {
            this.sendMessage("0");
        } catch (Exception e) {
            logger.error("连接异常！");
        }
    }

    /**
     * 服务端收到前端消息的处理
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        logger.info("用户信息: " + sessionId + "，报文" + message);
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                //群发消息
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    //将弹幕消息和对应的会话Id记录到消息体中
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocketService.getSessionId());
                    Message msg = new Message(MQConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    //生产者发送消息体
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }
                if (this.userId != null) {
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    //将弹幕记录异步写入数据库中
                    danmuService.asyncAddDanmu(danmu);
                    //将弹幕记录缓存到Redis中
                    danmuService.addDanmusToRedis(danmu);
                }
            } catch (Exception e) {
                logger.error("弹幕接收出现问题！");
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Throwable error) {

    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 定时任务
     * 每间隔5s，向各个客户端发送当前的视频的在线人数
     */
    @Scheduled(fixedRate = 5000)
    private void noticeOnlineCount() throws IOException {
        for (Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()) {
            WebSocketService webSocketService = entry.getValue();
            if (webSocketService.session.isOpen()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }

}

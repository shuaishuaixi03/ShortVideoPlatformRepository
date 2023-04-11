package com.wcx;


import com.wcx.video.service.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//开启事务管理
@EnableTransactionManagement
//开启异步
@EnableAsync
@EnableScheduling
//@EnableFeignClients(basePackages="")
//@EnableHystrix
public class wcxVideoApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(wcxVideoApp.class, args);
        WebSocketService.setApplicationContext(app);
    }
}

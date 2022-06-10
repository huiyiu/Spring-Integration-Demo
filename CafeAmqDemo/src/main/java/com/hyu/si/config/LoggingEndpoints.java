 /* 版权所有 ( c ) 2022。保留所有权利。
  *
  *
  * 项目：si
  * 文件名：MessageEndpoints
  * 描述：
  * 作者名：wanghy
  * 日期：22/6/6
  *
  * 修改历史：
  * 【时间】     【修改者】     【修改内容】
  *
  */
 package com.hyu.si.config;

 import org.springframework.context.annotation.Bean;
 import org.springframework.integration.annotation.MessageEndpoint;
 import org.springframework.integration.annotation.MessagingGateway;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.channel.QueueChannel;
 import org.springframework.integration.handler.LoggingHandler;
 import org.springframework.integration.handler.LoggingHandler.Level;
 import org.springframework.messaging.MessageChannel;
 import org.springframework.stereotype.Component;

 /**
  * @author wanghy
  */

 @MessageEndpoint
 @Component
 public class LoggingEndpoints {


     @Bean
     public MessageChannel logger() {
         return new QueueChannel();
     }


     @Bean
     @ServiceActivator(inputChannel = "logger")
     public LoggingHandler logging() {
         LoggingHandler adapter = new LoggingHandler(Level.INFO);
         adapter.setLoggerName("TEST_LOGGER");
         adapter.setLogExpressionString("headers.id + ': ' + payload");
         return adapter;
     }

     @MessagingGateway(defaultRequestChannel = "logger")
     public interface MyGateway {
         void sendToLogger(String data);
     }

    /* @Bean
     @GlobalChannelInterceptor(patterns = "orders,coldDrinks,hotDrinks,preparedDrinks,deliveries,errorChannel")
     public WireTap wireTap(MessageChannel logger) {
         return new WireTap(logger);
     }*/

     @ServiceActivator(inputChannel = "coldJsonDrinks")
     void handler(Object payload) {
         System.out.println("coldJsonDrinks");
         System.out.println(payload);
     }

     @ServiceActivator(inputChannel = "test")
     void handlerTest(Object payload) {
         System.out.println("test");
         System.out.println(payload);
     }

    @ServiceActivator(inputChannel = "preparedJsonDrinks")
     void handler2(Object payload) {
         System.out.println("preparedJsonDrinks");
         System.out.println(payload);
     }

     @ServiceActivator(inputChannel = "jsonDeliveries")
     void handler3(Object payload) {
         System.out.println("jsonDeliveries");
         System.out.println(payload);
     }

     @ServiceActivator(inputChannel = "deliveries")
     void handler4(Object payload) {
         System.out.println("deliveries");
         System.out.println(payload);
     }

 }

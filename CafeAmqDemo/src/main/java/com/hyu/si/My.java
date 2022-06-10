 /* 版权所有 ( c ) 2022。保留所有权利。
  *
  *
  * 项目：CafeAmqDemo
  * 文件名：My
  * 描述：
  * 作者名：wanghy
  * 日期：22/6/10
  *
  * 修改历史：
  * 【时间】     【修改者】     【修改内容】
  *
  */
 package com.hyu.si;

 import org.springframework.context.annotation.Bean;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.dsl.IntegrationFlow;
 import org.springframework.integration.dsl.IntegrationFlows;
 import org.springframework.integration.dsl.MessageChannels;
 import org.springframework.messaging.MessageChannel;
 import org.springframework.stereotype.Component;

 /**
  * @author wanghy
  */
 @Component
 public class My {
     @Bean
     public MessageChannel publishSubscribeChannel() {
         return MessageChannels.publishSubscribe().get();
     }

     @Bean
     public IntegrationFlow myFlow() {
         return IntegrationFlows.from("publishSubscribeChannel")
                 .<String>handle((p, h) -> p)
                 .handle(this)
                 .get();
     }

     @ServiceActivator
     public void handle(String payload) {
         System.out.println("publishSubscribeChannel:" + payload);
     }


     @Bean
     public IntegrationFlow controlBusFlow() {
         return IntegrationFlows.from(ControlBusGateway.class)
                 .handle(System.out::println)
                 .get();
     }

 }

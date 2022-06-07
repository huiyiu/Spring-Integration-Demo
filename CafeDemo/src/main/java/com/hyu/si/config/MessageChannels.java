 /* 版权所有 ( c ) 2022。保留所有权利。
  *
  *
  * 项目：si
  * 文件名：MessageChannels
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
 import org.springframework.context.annotation.Configuration;
 import org.springframework.integration.annotation.BridgeFrom;
 import org.springframework.integration.annotation.Poller;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.channel.DirectChannel;
 import org.springframework.integration.channel.QueueChannel;
 import org.springframework.messaging.MessageChannel;
 import org.springframework.messaging.SubscribableChannel;


/* <int:gateway id="cafe" service-interface="org.springframework.integration.samples.cafe.Cafe"/>

 <int:channel id="orders"/>
 <int:channel id="drinks"/>
 <int:channel id="coldDrinks"><int:queue capacity="10"/></int:channel>
 <int:channel id="hotDrinks"><int:queue capacity="10"/></int:channel>
 <int:channel id="coldDrinkBarista"/>
 <int:channel id="hotDrinkBarista"/>
 <int:channel id="preparedDrinks"/>

 <int:bridge input-channel="coldDrinks" output-channel="coldDrinkBarista">
 <int:poller fixed-delay="1000"/>
 </int:bridge>

 <int:bridge input-channel="hotDrinks" output-channel="hotDrinkBarista">
 <int:poller fixed-delay="1000"/>
 </int:bridge>

 <int-stream:stdout-channel-adapter id="deliveries"/>*/

 /**
  * @author wanghy
  */
 @Configuration
 public class MessageChannels {
     @Bean
     public MessageChannel orders() {
         return new DirectChannel();
     }

     @Bean
     public MessageChannel drinks() {
         return new DirectChannel();
     }

     @Bean
     public MessageChannel coldDrinks() {
         return new QueueChannel(10);
     }

     @Bean
     public MessageChannel hotDrinks() {
         return new QueueChannel(10);
     }

     /* @Bean
      public MessageChannel coldDrinkBarista() {
       return new DirectChannel();
      }*/
     /* @Bean
      public MessageChannel hotDrinkBarista() {
       return new DirectChannel();
      }*/
     @Bean
     public MessageChannel preparedDrinks() {
         return new DirectChannel();
     }

     @Bean
     @BridgeFrom(value = "coldDrinks", poller = @Poller(fixedDelay = "1000"))
     public SubscribableChannel coldDrinkBarista() {
         return new DirectChannel();
     }

     @Bean
     @BridgeFrom(value = "hotDrinks", poller = @Poller(fixedDelay = "1000"))
     public SubscribableChannel hotDrinkBarista() {
         return new DirectChannel();
     }

     /*@Bean
     @ServiceActivator(inputChannel = "hotDrinks",
             poller = @Poller(fixedRate = "1000"))
     public BridgeHandler bridge() {
         BridgeHandler bridge = new BridgeHandler();
         bridge.setOutputChannelName("hotDrinkBarista");
         return bridge;
     }*/

     @ServiceActivator(inputChannel = "deliveries")
     void handle(Object payload) {
         System.out.println(payload);
     }
 }

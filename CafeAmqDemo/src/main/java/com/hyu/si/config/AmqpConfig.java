 /* 版权所有 ( c ) 2022。保留所有权利。
  *
  *
  * 项目：CafeAmqDemo
  * 文件名：AmqpConfig
  * 描述：
  * 作者名：wanghy
  * 日期：22/6/7
  *
  * 修改历史：
  * 【时间】     【修改者】     【修改内容】
  *
  */
 package com.hyu.si.config;

 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
 import org.springframework.amqp.rabbit.core.RabbitTemplate;
 import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;

 /**
  * @author wanghy
  */
 @Configuration
 public class AmqpConfig {

  @Bean
  public CachingConnectionFactory rabbitConnectionFactory(){
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
    cachingConnectionFactory.setUsername("admin");
    cachingConnectionFactory.setPassword("Nuctech123");
    cachingConnectionFactory.setPublisherReturns(true);
    return cachingConnectionFactory;
   }

   @Bean
   public AmqpTemplate amqpTemplate(CachingConnectionFactory cachingConnectionFactory){
    RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
    //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    //rabbitTemplate.setMandatory(true);
     return rabbitTemplate;
   }

  @Bean
  public AmqpTemplate amqpTemplate2(CachingConnectionFactory cachingConnectionFactory){
   RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
   //rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
   rabbitTemplate.setReplyTimeout(10000);
   rabbitTemplate.setMandatory(true);
   return rabbitTemplate;
  }
 }

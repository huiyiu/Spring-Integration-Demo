 /* 版权所有 ( c ) 2022。保留所有权利。
  *
  *
  * 项目：CafeAmqDemo
  * 文件名：ColdChannels
  * 描述：
  * 作者名：wanghy
  * 日期：22/6/7
  *
  * 修改历史：
  * 【时间】     【修改者】     【修改内容】
  *
  */
 package com.hyu.si.config;

 import com.hyu.si.model.Drink;
 import com.hyu.si.model.OrderItem;
 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.amqp.core.Binding;
 import org.springframework.amqp.core.BindingBuilder;
 import org.springframework.amqp.core.Queue;
 import org.springframework.amqp.core.TopicExchange;
 import org.springframework.amqp.rabbit.connection.ConnectionFactory;
 import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.integration.amqp.inbound.AmqpInboundGateway;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.integration.channel.DirectChannel;
 import org.springframework.integration.handler.MessageHandlerChain;
 import org.springframework.integration.json.JsonToObjectTransformer;
 import org.springframework.integration.json.ObjectToJsonTransformer;
 import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;
 import org.springframework.integration.transformer.AbstractPayloadTransformer;
 import org.springframework.integration.transformer.MessageTransformingHandler;
 import org.springframework.messaging.MessageChannel;
 import org.springframework.messaging.MessageHandler;

 import java.util.ArrayList;
 import java.util.List;


/* <int-amqp:inbound-gateway
         id="coldDrinksBarista"
         request-channel="coldJsonDrinks"
         queue-names="cold-drinks"
         connection-factory="rabbitConnectionFactory" />

 <int:chain input-channel="coldJsonDrinks">
 <int:json-to-object-transformer type="org.springframework.integration.samples.cafe.OrderItem"/>
 <int:service-activator method="prepareColdDrink">
 <bean class="org.springframework.integration.samples.cafe.xml.Barista"/>
 </int:service-activator>
 <int:object-to-json-transformer content-type="text/x-json"/>
 </int:chain>

 <!-- rabbit exchanges, queues, and bindings used by this app -->
 <rabbit:topic-exchange name="cafe-drinks" auto-delete="true" durable="true">
 <rabbit:bindings>
 <rabbit:binding queue="cold-drinks" pattern="drink.cold"/>
 <rabbit:binding queue="all-cold-drinks" pattern="drink.cold"/>
 </rabbit:bindings>
 </rabbit:topic-exchange>

 <rabbit:queue name="cold-drinks" auto-delete="true" durable="true"/>
 <rabbit:queue name="all-cold-drinks" auto-delete="true" durable="true"/>*/

 /**
  * @author wanghy
  */
 @Configuration
 public class ColdChannels {

     @Bean
     public SimpleMessageListenerContainer listenerContainer(ConnectionFactory rabbitConnectionFactory) {
         SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitConnectionFactory);
         //container.setConcurrency("5");
         container.setQueueNames("cold-drinks");
         return container;
     }

     @Bean
     public MessageChannel coldJsonDrinks() {
         return new DirectChannel();
     }

    /* @Bean
     public AmqpInboundChannelAdapter inbound(SimpleMessageListenerContainer listenerContainer,
                                              MessageChannel coldJsonDrinks, AmqpTemplate amqpTemplate) {
         AmqpInboundChannelAdapter  adapter = new AmqpInboundChannelAdapter (listenerContainer);
         adapter.setOutputChannel(coldJsonDrinks);
         return adapter;
     }*/

     @Bean
     public AmqpInboundGateway inbound(SimpleMessageListenerContainer listenerContainer,
                                              MessageChannel coldJsonDrinks, AmqpTemplate amqpTemplate) {
         AmqpInboundGateway  gateway = new AmqpInboundGateway (listenerContainer);
         gateway.setRequestChannel(coldJsonDrinks);
         gateway.setDefaultReplyTo("");
         gateway.setReplyChannel(coldJsonDrinks);
         return gateway;
     }


     @ServiceActivator(inputChannel = "coldJsonDrinks")
     @Bean
     public MessageHandler chain() {
         MessageHandlerChain chain = new MessageHandlerChain();
        // MethodInvokingMessageHandler
         List<MessageHandler> handlers = new ArrayList<>();
         handlers.add(new MessageTransformingHandler(jsonToOrderItemTransformer()));
         handlers.add( new MessageTransformingHandler(prepareColdDrink()));
         handlers.add( new MessageTransformingHandler(orderToJsonTransformer()));
         chain.setHandlers(handlers);
         return chain;
     }

     @Bean
     public MessageChannel test() {
         return new DirectChannel();
     }

     @Transformer
     public JsonToObjectTransformer jsonToOrderItemTransformer() {
         return new JsonToObjectTransformer(OrderItem.class);
     }


     @Transformer
     public AbstractPayloadTransformer<?, ?> prepareColdDrink() {
         return new AbstractPayloadTransformer<OrderItem, Drink>() {

             @Override
             protected Drink transformPayload(OrderItem orderItem) {
                 return new Drink(orderItem.getOrderNumber(), orderItem.getDrinkType(), orderItem.isIced(),
                         orderItem.getShots());
             }
         };
     }


     @Transformer
     public ObjectToJsonTransformer orderToJsonTransformer() {
         return new ObjectToJsonTransformer(ResultType.STRING);
     }
     // chain --------------------end--------------------------------

     @Bean
     public Queue coldDrinksQueue() {
         return new Queue("cold-drinks", true,true,true);
     }

     @Bean
     public Queue allColdDrinksQueue() {
         return new Queue("all-cold-drinks", true,true,true);
     }

     @Bean
     public Binding bindingColdDrinksToCafeDrinksExchange(TopicExchange cafeDrinksExchange) {
         return BindingBuilder.bind(coldDrinksQueue()).to(cafeDrinksExchange).with("drink.cold");
     }

     @Bean
     public Binding bindingAllColdDrinksToCafeDrinksExchange(TopicExchange cafeDrinksExchange) {
         return BindingBuilder.bind(allColdDrinksQueue()).to(cafeDrinksExchange).with("drink.cold");
     }
 }

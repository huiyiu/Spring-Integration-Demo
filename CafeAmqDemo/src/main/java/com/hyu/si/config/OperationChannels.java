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

 import com.hyu.si.model.Delivery;
 import com.hyu.si.model.Drink;
 import com.hyu.si.model.Order;
 import com.hyu.si.model.OrderItem;
 import org.reactivestreams.Processor;
 import org.springframework.amqp.core.AcknowledgeMode;
 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.amqp.core.Binding;
 import org.springframework.amqp.core.BindingBuilder;
 import org.springframework.amqp.core.FanoutExchange;
 import org.springframework.amqp.core.Queue;
 import org.springframework.amqp.core.TopicExchange;
 import org.springframework.amqp.rabbit.connection.ConnectionFactory;
 import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
 import org.springframework.amqp.rabbit.support.ValueExpression;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.expression.Expression;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.integration.aggregator.AggregatingMessageHandler;
 import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
 import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
 import org.springframework.integration.aggregator.MethodInvokingMessageGroupProcessor;
 import org.springframework.integration.aggregator.SimpleMessageGroupProcessor;
 import org.springframework.integration.amqp.dsl.Amqp;
 import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
 import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
 import org.springframework.integration.annotation.Aggregator;
 import org.springframework.integration.annotation.BridgeFrom;
 import org.springframework.integration.annotation.Gateway;
 import org.springframework.integration.annotation.MessagingGateway;
 import org.springframework.integration.annotation.Poller;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.annotation.Splitter;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.integration.channel.DirectChannel;
 import org.springframework.integration.channel.QueueChannel;
 import org.springframework.integration.dsl.IntegrationFlows;
 import org.springframework.integration.handler.BridgeHandler;
 import org.springframework.integration.handler.MessageHandlerChain;
 import org.springframework.integration.handler.MethodInvokingMessageHandler;
 import org.springframework.integration.handler.ServiceActivatingHandler;
 import org.springframework.integration.json.JsonToObjectTransformer;
 import org.springframework.integration.json.ObjectToJsonTransformer;
 import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;
 import org.springframework.integration.router.HeaderValueRouter;
 import org.springframework.integration.scheduling.PollerMetadata;
 import org.springframework.integration.store.SimpleMessageStore;
 import org.springframework.integration.transformer.HeaderEnricher;
 import org.springframework.integration.transformer.MessageTransformingHandler;
 import org.springframework.integration.transformer.support.ExpressionEvaluatingHeaderValueMessageProcessor;
 import org.springframework.integration.transformer.support.HeaderValueMessageProcessor;
 import org.springframework.messaging.MessageChannel;
 import org.springframework.messaging.MessageHandler;
 import org.springframework.messaging.SubscribableChannel;
 import org.springframework.scheduling.support.PeriodicTrigger;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.stream.Collectors;

 /*<!--  intercept messages on these channels while still allowing them to continue -->
 <int:wire-tap channel="logger" pattern="orders,coldDrinks,hotDrinks,preparedDrinks,deliveries"/>
 <int:logging-channel-adapter id="logger" log-full-message="true" level="INFO"/>

 <!--  To receive AMQP Messages from a Queue, configure an inbound-channel-adapter  -->
 <int-amqp:inbound-channel-adapter queue-names="new-orders" channel="jsonOrders" connection-factory="rabbitConnectionFactory" acknowledge-mode="AUTO" />

 <int:json-to-object-transformer id="json-to-order" input-channel="jsonOrders" output-channel="preOrders" type="org.springframework.integration.samples.cafe.Order" />

 <int:splitter input-channel="preOrders" expression="payload.items" output-channel="preDrinks" apply-sequence="true"/>

 <int:header-enricher input-channel="preDrinks" output-channel="drinks">
 <int:header name="ICED" expression="payload.isIced()"/>
 </int:header-enricher>

 <int:object-to-json-transformer id="drink-to-json" input-channel="drinks" output-channel="jsonDrinks" content-type="text/x-json"/>

 <int:router input-channel="jsonDrinks"  expression="headers.ICED ? 'coldDrinks' : 'hotDrinks'"/>

 <int:channel id="coldDrinks">
 <int:queue/>
 </int:channel>

 <!-- Default poller -->
 <int:poller default="true" fixed-rate="100"/>

 <int:channel id="hotDrinks">
 <int:queue/>
 </int:channel>

 <!-- To send AMQP Messages to an Exchange and receive back a response from a remote client, configure an outbound-gateway -->
 <int-amqp:outbound-gateway
         id="coldDrinksBarista"
         request-channel="coldDrinks"
         reply-channel="preparedJsonDrinks"
         exchange-name="cafe-drinks"
         routing-key="drink.cold"
         amqp-template="amqpTemplate" />

 <!-- To send AMQP Messages to an Exchange and receive back a response from a remote client, configure an outbound-gateway -->
 <int-amqp:outbound-gateway
         id="hotDrinksBarista"
         request-channel="hotDrinks"
         reply-channel="preparedJsonDrinks"
         exchange-name="cafe-drinks"
         routing-key="drink.hot"
         amqp-template="amqpTemplate" />

 <int:channel id="preparedJsonDrinks"/>

 <int:json-to-object-transformer id="json-to-drink" input-channel="preparedJsonDrinks" output-channel="preparedDrinks" type="org.springframework.integration.samples.cafe.Drink"/>

 <int:aggregator input-channel="preparedDrinks"  method="prepareDelivery" output-channel="preDeliveries">
 <bean class="org.springframework.integration.samples.cafe.xml.Waiter"/>
 </int:aggregator>

 <int:channel id="preDeliveries" />

 <int:header-enricher input-channel="preDeliveries" output-channel="deliveries">
 <int:header name="NUMBER" expression="payload.getOrderNumber()" />
 </int:header-enricher>

 <int:object-to-json-transformer id="delivery-to-json" input-channel="deliveries" output-channel="jsonDeliveries" content-type="text/x-json"/>

 <int:channel id="jsonDeliveries" />

 <!--  To send AMQP Messages to an Exchange, configure an outbound-channel-adapter. -->
 <int-amqp:outbound-channel-adapter
         id="deliveredOrders"
         channel="jsonDeliveries"
         amqp-template="amqpTemplate"
         exchange-name="cafe-deliveries"
         routing-key-expression="'delivery.'+headers.NUMBER" />

 <bean id="waiter" class="org.springframework.integration.samples.cafe.xml.Waiter"/>

 <!-- rabbit exchanges, queues, and bindings used by this app -->
 <rabbit:topic-exchange name="cafe-drinks" auto-delete="true" durable="true">
 <rabbit:bindings>
 <rabbit:binding queue="all-drinks" pattern="drink.*"/>
 </rabbit:bindings>
 </rabbit:topic-exchange>

 <rabbit:queue name="all-drinks" auto-delete="true" durable="true"/>

 <rabbit:fanout-exchange name="cafe-deliveries" auto-delete="false" durable="true">
 <rabbit:bindings>
 <rabbit:binding queue="all-deliveries" />
 </rabbit:bindings>
 </rabbit:fanout-exchange>

 <rabbit:queue name="all-deliveries" auto-delete="false" durable="true"/>
 <rabbit:queue name="new-orders" auto-delete="false" durable="true"/>*/

 /**
  * @author wanghy
  */
 @Configuration
 public class OperationChannels {

     @Autowired
     AmqpTemplate amqpTemplate2;

     @Bean
     public SimpleMessageListenerContainer container(ConnectionFactory rabbitConnectionFactory) {
         SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitConnectionFactory);
         container.setAcknowledgeMode(AcknowledgeMode.AUTO);
         container.setQueueNames("new-orders");
         return container;
     }

     @Bean
     public MessageChannel jsonOrders() {
         return new DirectChannel();
     }

    @Bean
    public AmqpInboundChannelAdapter jsonOrdersChannelAdapter(SimpleMessageListenerContainer container,
                                                              MessageChannel jsonOrders) {
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
        adapter.setOutputChannel(jsonOrders);
        return adapter;
    }

     @Bean
     @Transformer(inputChannel = "jsonOrders",outputChannel = "preOrders")
     public JsonToObjectTransformer jsonToOrderTransformer() {
         return new JsonToObjectTransformer(Order.class);
     }

     @Splitter(inputChannel="preOrders", outputChannel="preDrinks",applySequence="true")
     public List<OrderItem> orderSplitter(Order order) {
         return order.getItems();
     }

     @Bean
     @Transformer(inputChannel="preDrinks", outputChannel="drinks")
     public HeaderEnricher enrichDrinksHeaders() {
         Map<String, HeaderValueMessageProcessor<?>> headersToAdd = new HashMap<>();
         Expression expression = new SpelExpressionParser().parseExpression("payload.isIced()");
         headersToAdd.put("ICED",
                 new ExpressionEvaluatingHeaderValueMessageProcessor<>(expression, Boolean.class));
         HeaderEnricher enricher = new HeaderEnricher(headersToAdd);
         return enricher;
     }

     @Bean
     @Transformer(inputChannel = "drinks",outputChannel = "jsonDrinks")
     public ObjectToJsonTransformer drinkTpoJsonToTransformer() {
         return new ObjectToJsonTransformer(ResultType.STRING);
     }

    @ServiceActivator(inputChannel = "jsonDrinks")
    @Bean
    public HeaderValueRouter router() {
        HeaderValueRouter router = new HeaderValueRouter("ICED");
        router.setChannelMapping("true", "coldDrinks");
        router.setChannelMapping("false", "hotDrinks");
        return router;
    }

     @Bean(PollerMetadata.DEFAULT_POLLER)
     public PollerMetadata defaultPoller() {
         PollerMetadata pollerMetadata = new PollerMetadata();
         pollerMetadata.setTrigger(new PeriodicTrigger(100));
         return pollerMetadata;
     }

     @Bean
     public MessageChannel coldDrinks() {
         return new QueueChannel();
     }

     @Bean
     public MessageChannel hotDrinks() {
         return new QueueChannel();
     }

     @Bean
     public MessageChannel preparedJsonDrinks() {
        return new DirectChannel();
    }


     @Bean
     @ServiceActivator(inputChannel = "coldDrinks")
     public AmqpOutboundEndpoint coldDrinksEndpoint() {
         AmqpOutboundEndpoint outbound = new AmqpOutboundEndpoint(amqpTemplate2);
         outbound.setExchangeName("cafe-drinks");
         outbound.setRoutingKey("drink.cold");
         outbound.setOutputChannelName("preparedJsonDrinks");
         return outbound;
     }

     @MessagingGateway
     public interface MyGateway {
         @Gateway(requestChannel = "coldDrinks",replyChannel = "preparedJsonDrinks",payloadExpression = "payload")
         Object sendToRabbit(Object data);
     }

    /* @ServiceActivator(inputChannel = "coldDrinks",outputChannel = "preparedJsonDrinks",requiresReply = "true")
     public Object coldDrinksPayload(Object payload) {
         return payload;
     }*/

     /*	<int:chain input-channel="coldDrinks" output-channel="deliveries">
		<int:json-to-object-transformer type="org.springframework.integration.samples.cafe.Drink"/>
		<int:aggregator method="prepareDelivery">
			<bean class="org.springframework.integration.samples.cafe.xml.Waiter"/>
		</int:aggregator>
		<int:header-enricher>
			<int:header name="NUMBER" expression="payload.getOrderNumber()"/>
		</int:header-enricher>
	</int:chain>*/

     @ServiceActivator(inputChannel = "coldDrinks")
     @Bean
     public MessageHandler chainCode() {
         MessageHandlerChain chain = new MessageHandlerChain();
         List<MessageHandler> handlers = new ArrayList<>();
         handlers.add(new MessageTransformingHandler(jsonToDrinkTransformer()));
         // aggregating 无效
        /* AggregatingMessageHandler aggregatingMessageHandler = new AggregatingMessageHandler(
                 new MethodInvokingMessageGroupProcessor( helpers(),"prepareDelivery"));*/
         //handlers.add(aggregatingMessageHandler);
         handlers.add( new MessageTransformingHandler(deliveriesHeaders()));
         chain.setHandlers(handlers);
         chain.setOutputChannelName("deliveries");
         return chain;
     }

     @Bean
     public OrderAggregator helpers() {
         return new OrderAggregator();
     }

     public static class OrderAggregator {
         //@Aggregator(inputChannel = "preparedDrinks", outputChannel = "preDeliveries",autoStartup="true")
         public Delivery prepareDelivery(List<Drink> drinks) {
             return new Delivery(drinks);
         }
     }

     @Transformer
     public JsonToObjectTransformer jsonToDrinkTransformer() {
         return new JsonToObjectTransformer(Drink.class);
     }


     @Transformer
     public HeaderEnricher deliveriesHeaders() {
         Map<String, HeaderValueMessageProcessor<?>> headersToAdd = new HashMap<>();
         Expression expression = new SpelExpressionParser().parseExpression("payload.getOrderNumber()");
         headersToAdd.put("NUMBER",
                 new ExpressionEvaluatingHeaderValueMessageProcessor<>(expression, Integer.class));
         HeaderEnricher enricher = new HeaderEnricher(headersToAdd);


         return enricher;
     }

     @Bean
     public MessageChannel deliveries() {
         return new DirectChannel();
     }

     @Bean
     @Transformer(inputChannel = "deliveries",outputChannel = "jsonDeliveries")
     public ObjectToJsonTransformer deliveryToJsonToTransformer() {
         return new ObjectToJsonTransformer(ResultType.STRING);
     }

     @Bean
     public MessageChannel jsonDeliveries() {
         return new DirectChannel();
     }

     @Bean
     @ServiceActivator(inputChannel = "jsonDeliveries")
     public AmqpOutboundEndpoint deliveredOrders(AmqpTemplate amqpTemplate) {
         AmqpOutboundEndpoint outbound = new AmqpOutboundEndpoint(amqpTemplate);
         outbound.setExchangeName("cafe-deliveries");
         outbound.setRoutingKeyExpressionString("'delivery.'+headers.NUMBER");
         return outbound;
     }

     @Bean
     public MessageChannel amqpOutboundChannel() {
         return new DirectChannel();
     }

     @Bean
     public TopicExchange cafeDrinksExchange() {
         return new TopicExchange("cafe-drinks", true, true);
     }

     @Bean
     public Queue allDrinksQueue() {
         return new Queue("all-drinks", true,true,true);
     }

     @Bean
     public Binding bindingAllDrinksToCafeDrinksExchange() {
         return BindingBuilder.bind(allDrinksQueue()).to(cafeDrinksExchange()).with("drink.*");
     }

     @Bean
     public FanoutExchange cafeDeliveriesExchange() {
         return new FanoutExchange("cafe-deliveries", true, false);
     }

     @Bean
     public Queue allDeliveriesQueue() {
         return new Queue("all-deliveries", true);
     }

     @Bean
     public Binding bindingAllDeliveriesToCafeDeliveriesExchange() {
         return BindingBuilder.bind(allDeliveriesQueue()).to(cafeDeliveriesExchange());
     }
 }
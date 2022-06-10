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

 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.amqp.core.Binding;
 import org.springframework.amqp.core.BindingBuilder;
 import org.springframework.amqp.core.Queue;
 import org.springframework.amqp.core.TopicExchange;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.expression.Expression;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
 import org.springframework.integration.annotation.MessagingGateway;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.integration.channel.DirectChannel;
 import org.springframework.integration.json.ObjectToJsonTransformer;
 import org.springframework.integration.json.ObjectToJsonTransformer.ResultType;
 import org.springframework.integration.transformer.HeaderEnricher;
 import org.springframework.integration.transformer.support.ExpressionEvaluatingHeaderValueMessageProcessor;
 import org.springframework.integration.transformer.support.HeaderValueMessageProcessor;
 import org.springframework.messaging.MessageChannel;

 import java.util.HashMap;
 import java.util.Map;


/* <int:channel id="orders"/>

	<int:header-enricher input-channel="orders" output-channel="newOrders">
		<int:header name="NUMBER" expression="payload.getNumber()"/>
	</int:header-enricher>

<int:object-to-json-transformer input-channel="newOrders" output-channel="jsonNewOrders" content-type="text/x-json"/>

	<int:channel id="jsonNewOrders" />

	<int-amqp:outbound-channel-adapter
		channel="jsonNewOrders"
		exchange-name="cafe-orders"
		routing-key-expression="'order.'+headers.NUMBER"
		amqp-template="amqpTemplate" />

	<rabbit:topic-exchange name="cafe-orders" auto-delete="false" durable="true">
		<rabbit:bindings>
			<rabbit:binding queue="new-orders" pattern="order.*"/>
			<rabbit:binding queue="all-orders" pattern="order.*"/>
		</rabbit:bindings>
	</rabbit:topic-exchange>

	<rabbit:queue name="new-orders" auto-delete="false" durable="true"/>
	<rabbit:queue name="all-orders" auto-delete="false" durable="true"/>

*/

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
     @Transformer(inputChannel="orders", outputChannel="newOrders")
     public HeaderEnricher enrichHeaders() {
         Map<String, HeaderValueMessageProcessor<?>> headersToAdd = new HashMap<>();
         Expression expression = new SpelExpressionParser().parseExpression("payload.getNumber()");
         headersToAdd.put("NUMBER",
                 new ExpressionEvaluatingHeaderValueMessageProcessor<>(expression, Integer.class));
         HeaderEnricher enricher = new HeaderEnricher(headersToAdd);
         return enricher;
     }

     @Bean
     @Transformer(inputChannel = "newOrders", outputChannel = "jsonNewOrders")
     public ObjectToJsonTransformer objectToJsonTransformer() {
         return new ObjectToJsonTransformer(ResultType.STRING);
     }

     @Bean
     public MessageChannel jsonNewOrders() {
         return new DirectChannel();
     }

     @Bean
     @ServiceActivator(inputChannel = "jsonNewOrders")
     public AmqpOutboundEndpoint amqpOutbound(AmqpTemplate amqpTemplate) {
         AmqpOutboundEndpoint outbound = new AmqpOutboundEndpoint(amqpTemplate);
         outbound.setExchangeName("cafe-orders");
         outbound.setRoutingKeyExpressionString("'order.'+headers.NUMBER");
         return outbound;
     }

     @MessagingGateway(defaultRequestChannel = "jsonNewOrders")
     public interface MyGateway2 {
         String sendToRabbit(String data);
     }

     @Bean
     public Queue newOrdersQueue() {
         return new Queue("new-orders", true);
     }
     @Bean
     public Queue allOrdersQueue() {
         return new Queue("all-orders", true);
     }

     @Bean
     public TopicExchange cafeOrdersExchange() {
         return new TopicExchange("cafe-orders", true, false);
     }

     @Bean
     public Binding bindingNewOrdersToCafeOrdersExchange(Queue newOrdersQueue,TopicExchange cafeOrdersExchange) {
         return BindingBuilder.bind(newOrdersQueue).to(cafeOrdersExchange).with("order.*");
     }
     @Bean
     public Binding bindingAllOrdersToCafeOrdersExchange(Queue allOrdersQueue,TopicExchange cafeOrdersExchange) {
         return BindingBuilder.bind(allOrdersQueue).to(cafeOrdersExchange).with("order.*");
     }
 }
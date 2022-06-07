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

 import com.hyu.si.model.Order;
 import com.hyu.si.model.OrderItem;
 import org.springframework.integration.annotation.MessageEndpoint;
 import org.springframework.integration.annotation.Router;
 import org.springframework.integration.annotation.Splitter;

 import java.util.List;

 /**
  * @author wanghy
  */

 @MessageEndpoint
 public class MessageEndpoints {


  /**
   * drinks router
   * @param orderItem
   * @return
   */
  @Router(inputChannel="drinks")
  public String drinkRouter(OrderItem orderItem) {
   return (orderItem.isIced()) ? "coldDrinks" : "hotDrinks";
  }

  /**
   * order splitter
   * @param order
   * @return
   */
  @Splitter(inputChannel="orders", outputChannel="drinks")
  public List<OrderItem> orderSplitter(Order order) {
   return order.getItems();
  }

 }

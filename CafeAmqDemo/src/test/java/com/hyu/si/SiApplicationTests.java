package com.hyu.si;

import com.hyu.si.config.CafeGateWay;
import com.hyu.si.model.DrinkType;
import com.hyu.si.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SiApplicationTests {

    @Autowired
    CafeGateWay cafeGateWay;
    @Autowired
    MyFlowAdapter myFlowAdapter;
    @Autowired
    ControlBusGateway controlBusGateway;

    @Test
    void contextLoads() {
       /* for (int i = 1; i <= 20; i++) {
            Order order = new Order(i);
            order.addItem(DrinkType.LATTE, 2, false);
            order.addItem(DrinkType.MOCHA, 3, true);
            cafeGateWay.placeOrder(order);
        }*/
        //myFlowAdapter.buildFlow();
        controlBusGateway.send("123");
    }

}

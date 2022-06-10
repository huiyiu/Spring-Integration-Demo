package com.hyu.si;

import com.hyu.si.model.Drink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.ReleaseStrategy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.TriggerContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class MyFlowAdapter extends IntegrationFlowAdapter {

    private final AtomicBoolean invoked = new AtomicBoolean();

    public Date nextExecutionTime(TriggerContext triggerContext) {
        return this.invoked.getAndSet(true) ? null : new Date();
    }

    @Override
    protected IntegrationFlowDefinition<?> buildFlow() {
        return from(this::messageSource, e -> e.poller(p -> p.trigger(this::nextExecutionTime)))
                .split(this)
                .bridge()
                .channel("publishSubscribeChannel")
                .transform(this)
                .aggregate(this)
                .enrichHeaders(Collections.singletonMap("thing1", "THING1"))
                .filter(this)
                .handle(this)
                .channel(c -> c.queue("myFlowAdapterOutput"));
    }


    public Message<Object> messageSource() {
        //return "T,H,I,N,G,2";
        Object o = "T,H,I,N,G,2,T";
        return MessageBuilder.withPayload(o).build();
    }

    @Splitter
    public String[] split(String payload) {
        return StringUtils.commaDelimitedListToStringArray(payload);
    }

    @Transformer
    public String transform(String payload) {
        return payload.toLowerCase();
    }

    @Aggregator
    public String aggregate(List<String> payloads) {
        return payloads.stream().collect(Collectors.joining());
    }

    @CorrelationStrategy
    public Object correlatingFor(String message) {
        return message;
    }

    @ReleaseStrategy
    public Boolean release(List<String> payloads) {
        return payloads != null && payloads.size() == 2;
    }

    @Filter
    public boolean filter(@Header Optional<String> thing1) {
        return thing1.isPresent();
    }

    @ServiceActivator
    public String handle(String payload, @Header String thing1) {
        System.out.println(payload + ":" + thing1);
        return payload + ":" + thing1;
    }

}
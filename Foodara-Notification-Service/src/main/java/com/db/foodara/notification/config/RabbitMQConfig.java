package com.db.foodara.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "foodara.events";

    // Queues
    public static final String ORDER_PLACED_QUEUE = "notification.order.placed";
    public static final String ORDER_STATUS_QUEUE = "notification.order.status";
    public static final String PAYMENT_COMPLETED_QUEUE = "notification.payment.completed";
    public static final String ORDER_CANCELLED_QUEUE = "notification.order.cancelled";
    public static final String SYSTEM_NOTIFICATION_QUEUE = "notification.system";
    public static final String USER_REGISTERED_QUEUE = "notification.user.registered";

    // Routing keys
    public static final String ORDER_PLACED_KEY = "order.placed";
    public static final String ORDER_STATUS_KEY = "order.status";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String SYSTEM_NOTIFY_KEY = "system.notify";
    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String REFUND_VOUCHER_KEY = "refund.voucher";
    public static final String REFUND_BANK_KEY = "refund.bank";

    @Bean
    public TopicExchange foodaraExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE).build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE).build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build();
    }

    @Bean
    public Queue systemNotificationQueue() {
        return QueueBuilder.durable(SYSTEM_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable("foodara.notification.refund").build();
    }

    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(exchange).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderStatusQueue).to(exchange).with(ORDER_STATUS_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentCompletedQueue).to(exchange).with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(exchange).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding systemNotifyBinding(Queue systemNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(systemNotificationQueue).to(exchange).with(SYSTEM_NOTIFY_KEY);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(exchange).with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding refundVoucherBinding(Queue refundQueue, TopicExchange exchange) {
        return BindingBuilder.bind(refundQueue).to(exchange).with(REFUND_VOUCHER_KEY);
    }

    @Bean
    public Binding refundBankBinding(Queue refundQueue, TopicExchange exchange) {
        return BindingBuilder.bind(refundQueue).to(exchange).with(REFUND_BANK_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}

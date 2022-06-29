package com.example.employee.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
public class RabbitmqConfig {
    


    public static final String QueueName = "request";
    public static final String ReplyQueue = "reply";
    public static final String Exchange = "employee_exchange_1";
    public static final String RoutingKey = "employee_routingKey_1";
    public static final String ReplyRoutingKey = "employee_reply_routingKey_1";
    private String username = "guest";
    private String password = "guest";
    private String host = "localhost";
    private int port = 5672;


    
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue queue() {
        return new Queue(QueueName);
    }

    // @Bean
    // Queue replyQueue() {
    //     return new Queue(ReplyQueue);
    // }

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(Exchange);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RoutingKey);
    }

    // @Bean
    // public Binding replyBinding() {
    //     return BindingBuilder.bind(replyQueue()).to(exchange()).with(ReplyRoutingKey);
    // }

    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate template(ConnectionFactory connectionFactory) {
        
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        rabbitTemplate.setReceiveTimeout(10000);
        // rabbitTemplate.setReplyAddress(ReplyQueue);
        // rabbitTemplate.setReplyTimeout(10000);
        return rabbitTemplate;
    }

    
    @Bean CachingConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        
        return connectionFactory;
    }

    @Bean
    SimpleMessageListenerContainer container() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        
        container.setConnectionFactory(connectionFactory());
        container.setQueues(queue());
        container.setMessageListener(template(connectionFactory()));
        return container;
    }

    // @Bean
    // SimpleMessageListenerContainer replyContainer() {
    //     SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory());
    //     container.setQueues(replyQueue());
    //     container.setMessageListener(template(connectionFactory()));
    //     return container;
    // }


}

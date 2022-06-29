package com.example.employee.config;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import org.apache.tomcat.util.json.JSONParser;

import com.example.employee.entity.Employee;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitmqRPCClient implements AutoCloseable {

    private String requestQueueName = RabbitmqConfig.QueueName;
    private Connection connection;
    private Channel channel;
    private String replyQueueName = RabbitmqConfig.ReplyQueue;

    public RabbitmqRPCClient() throws IOException, TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(replyQueueName, false, false, false, null);
        channel.queuePurge(replyQueueName);
    }

    public String call(Employee employee) throws IOException, InterruptedException{
        
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        
        channel.basicPublish("", requestQueueName, props, employee.toString().getBytes("UTF-8"));
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });
        
        String result = response.take();
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }
}

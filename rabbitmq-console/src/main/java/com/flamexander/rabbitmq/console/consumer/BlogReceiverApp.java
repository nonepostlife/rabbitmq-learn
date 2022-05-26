package com.flamexander.rabbitmq.console.consumer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class BlogReceiverApp {
    private static final String EXCHANGE_NAME = "blogExchanger";

    public static void main(String[] argv) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> themes = new ArrayList<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();

        new Thread(() -> {
            while (true) {
                String input = scanner.nextLine();
                if (!check(input)) {
                    System.out.println("Wrong format! Correct format: command theme");
                    continue;
                }
                String[] elements = input.split("\\s+");
                StringBuilder sb = new StringBuilder();
                String command = elements[0];
                String theme = elements[1];

                try {
                    if (command.equals("set_topic")) {
                        if (themes.contains(theme)) {
                            System.out.println("You are already subscribed to this topic");
                        } else {
                            themes.add(theme);
                            channel.queueBind(queueName, EXCHANGE_NAME, theme);
                            System.out.println("You subscribed to the topic - " + theme);
                        }
                        continue;
                    }
                    if (command.equals("remove_topic")) {
                        if (themes.contains(theme)) {
                            themes.remove(theme);
                            channel.queueUnbind(queueName, EXCHANGE_NAME, theme);
                            System.out.println("You unsubscribed from this topic - " + theme);
                        } else {
                            System.out.println("You are not subscribed to this topic");
                        }
                        continue;
                    }
                    System.out.println("Unknown command, available commands: set_topic, remove_topic");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        System.out.println(" [*] Waiting for messages");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    private static boolean check(String command) {
        return Pattern.compile("^[a-z_-]+\\s{1}\\w+$")
                .matcher(command)
                .matches();
    }
}

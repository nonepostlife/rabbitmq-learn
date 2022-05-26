package com.flamexander.rabbitmq.console.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;
import java.util.regex.Pattern;

public class BlogProducerApp {
    private static final String EXCHANGE_NAME = "blogExchanger";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Scanner scanner = new Scanner(System.in);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            while (true) {
                System.out.print("Print command: ");
                String command = scanner.nextLine();
                if (!check(command)) {
                    System.out.println("Wrong format! Correct format: 'theme' 'message'");
                    continue;
                }
                String[] elements = command.split("\\s+", 2);
                channel.basicPublish(EXCHANGE_NAME, elements[0], null, elements[1].toString().getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + elements[1] + "'");
            }
        }
    }

    private static boolean check(String command) {
        return Pattern.compile("^\\w+\\s{1}(.*\\s+)*.*\\S$")
                .matcher(command)
                .matches();
    }
}
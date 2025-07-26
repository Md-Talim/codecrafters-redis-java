package redis;

import java.io.IOException;
import java.net.ServerSocket;

import redis.configuration.Configuration;
import redis.store.Storage;

public class Main {
    public static final int PORT = 6379;

    public static void main(String[] args) {
        final var threadFactory = Thread.ofVirtual().factory();
        final var storage = new Storage();
        final var configuration = new Configuration();

        for (int i = 0; i < args.length; i += 2) {
            var key = args[i].substring(2); // remove -- from arg
            var value = args[i + 1];

            var property = configuration.getProperty(key);
            if (property == null) {
                System.err.println("unknown property: %s".formatted(key));
            } else {
                property.set(value);
            }
        }

        try (final var serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                final var socket = serverSocket.accept();
                final var client = new Client(socket, storage, configuration);
                final var thread = threadFactory.newThread(client);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

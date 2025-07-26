package redis;

import java.io.IOException;
import java.net.ServerSocket;

import redis.store.Storage;

public class Main {
    public static final int PORT = 6379;

    public static void main(String[] args) {
        final var threadFactory = Thread.ofVirtual().factory();
        final var storage = new Storage();

        try (final var serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                final var socket = serverSocket.accept();
                final var client = new Client(socket, storage);
                final var thread = threadFactory.newThread(client);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

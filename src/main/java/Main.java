import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static final int PORT = 6379;

    public static void main(String[] args) {
        final var threadFactory = Thread.ofVirtual().factory();

        try (final var serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                final var client = serverSocket.accept();
                final var thread = threadFactory.newThread(new Client(client));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}

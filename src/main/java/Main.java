import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PORT = 6379;

    public static void main(String[] args) {
        Socket clientSocket = null;

        try (final var serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept();
            var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            var outputStream = clientSocket.getOutputStream();

            String line;
            while ((line = reader.readLine()) != null) {
                if ("PING".equalsIgnoreCase(line)) {
                    outputStream.write("+PONG\r\n".getBytes());
                }
            }

            outputStream.flush();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}

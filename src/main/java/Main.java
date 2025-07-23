import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PORT = 6379;

    public static void main(String[] args) {
        Socket clientSocket = null;

        try (final var serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept();

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write("+PONG\r\n".getBytes());
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

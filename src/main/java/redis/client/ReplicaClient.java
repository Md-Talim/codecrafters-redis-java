package redis.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import redis.Redis;
import redis.rdb.RDBLoader;
import redis.resp.Deserializer;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;

public class ReplicaClient implements Runnable {

    private final Socket socket;
    private final Redis redis;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Deserializer deserializer;

    public ReplicaClient(Socket socket, Redis redis) throws IOException {
        this.socket = socket;
        this.redis = redis;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        deserializer = new Deserializer(inputStream);
    }

    @Override
    public void run() {
        try (socket) {
            handshake();

            RValue request;
            while ((request = deserializer.read()) != null) {
                System.out.println("replica: recieved: %s".formatted(request));
                var response = redis.evaluate(null, request);

                if (response == null) {
                    System.out.println("replica: no answer");
                    continue;
                }

                System.out.println(response.toString());
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void handshake() throws IOException {
        send(List.of(new BulkString("PING")));

        var masterPort = redis
            .configuration()
            .port()
            .getArgumentAt(0, Integer.class)
            .value();

        send(
            List.of(
                new BulkString("REPLCONF"),
                new BulkString("listening-port"),
                new BulkString(String.valueOf(masterPort))
            )
        );

        send(
            List.of(
                new BulkString("REPLCONF"),
                new BulkString("capa"),
                new BulkString("psync2")
            )
        );

        send(
            List.of(
                new BulkString("PSYNC"),
                new BulkString("?"),
                new BulkString("-1")
            )
        );

        var rdb = deserializer.readRDB();
        redis.storage().clear();
        RDBLoader.load(new ByteArrayInputStream(rdb), redis.storage());
    }

    private void send(List<RValue> args) throws IOException {
        System.out.println("replica: sending %s".formatted(args));
        outputStream.write(new RArray((args)).serialize());

        var response = deserializer.read();
        System.out.println("replica: received %s".formatted(response));
    }
}

package redis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import redis.configuration.Argument;
import redis.configuration.Configuration;
import redis.configuration.RemoteOption;
import redis.rdb.RDBLoader;
import redis.resp.Deserializer;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.store.Storage;

public class Main {
    public static void main(String[] args) throws IOException {
        final var threadFactory = Thread.ofVirtual().factory();
        final var storage = new Storage();
        final var configuration = new Configuration();

        for (int i = 0; i < args.length; ++i) {
            var key = args[i].substring(2); // remove -- from arg

            var option = configuration.getOption(key);
            if (option == null) {
                System.err.println("unknown property: %s".formatted(key));
                continue;
            }

            if ("replicaof".equalsIgnoreCase(key)) {
                String[] value = args[i + 1].split(" ");
                Argument<?> host = option.getArgumentAt(0);
                Argument<?> port = option.getArgumentAt(1);
                host.set(value[0]);
                port.set(value[1]);
                i++;
                continue;
            }

            int argumentsCount = option.argumentsCount();
            for (int j = 0; j < argumentsCount; ++j) {
                Argument<?> argument = option.getArgumentAt(j);
                String argumentValue = args[i + 1 + j];
                argument.set(argumentValue);
            }

            i += argumentsCount;
        }

        for (final var option : configuration.options()) {
            final var arguments = option.arguments()
                    .stream()
                    .map((argument) -> "%s=`%s`".formatted(argument.key(), argument.value()))
                    .collect(Collectors.joining(", "));

            System.out.println("configuration: %s(%s)".formatted(option.name(), arguments));
        }

        boolean isSlave = configuration.isSlave();
        if (isSlave) {
            connectToMaster(configuration.replicaOf());
        } else {
            loadRDBfile(storage, configuration);
        }

        final int port = configuration.port().getArgumentAt(0, Integer.class).value();
        System.out.println("port: %s".formatted(port));

        try (final var serverSocket = new ServerSocket(port)) {
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

    private static void loadRDBfile(final Storage storage, final Configuration configuration) {
        Argument<String> directory = configuration.directory().getPathArgument();
        Argument<String> dbFilename = configuration.databaseFilename().getPathArgument();
        if (directory.isSet() && dbFilename.isSet()) {
            var path = Paths.get(directory.value(), dbFilename.value());
            if (Files.exists(path)) {
                RDBLoader.load(path, storage);
            }
        }
    }

    private static void connectToMaster(RemoteOption replicaOf) {
        try (final var socket = new Socket(replicaOf.hostArgument().value(), replicaOf.portArgument().value())) {
            final var inputStream = socket.getInputStream();
            final var outputStream = socket.getOutputStream();

            final var deserializer = new Deserializer(inputStream);
            final var pingCommand = new BulkString("PING");
            outputStream.write(new RArray(List.of(pingCommand)).serialize());

            var response = deserializer.read();
            System.out.println("replica: received %s".formatted(response.toString()));
        } catch (IOException e) {
        }
    }
}

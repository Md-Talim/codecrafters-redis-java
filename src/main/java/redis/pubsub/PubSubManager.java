package redis.pubsub;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import redis.client.Client;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;

public class PubSubManager {

    // Channel name -> Set of subscribed clients
    private final Map<String, Set<Client>> channelSubscriptions;

    // Client -> Set of channels they're subscribed to
    private final Map<Client, Set<String>> clientChannels;

    public PubSubManager() {
        channelSubscriptions = new ConcurrentHashMap<>();
        clientChannels = new ConcurrentHashMap<>();
    }

    public synchronized void subscribe(Client client, String channel) {
        channelSubscriptions
            .computeIfAbsent(channel, _ -> new CopyOnWriteArraySet<>())
            .add(client);

        clientChannels
            .computeIfAbsent(client, _ -> new CopyOnWriteArraySet<>())
            .add(channel);

        client.incrementSubscriptionCount();
        client.enterSubscribedMode();
    }

    public synchronized int publish(String channel, String message) {
        var clients = channelSubscriptions.get(channel);
        if (clients == null) {
            return 0;
        }

        RArray messageResponse = new RArray(
            List.of(
                new BulkString("message"),
                new BulkString(channel),
                new BulkString(message)
            )
        );

        for (Client client : clients) {
            client.notifySubscription(messageResponse);
        }

        return clients.size();
    }
}

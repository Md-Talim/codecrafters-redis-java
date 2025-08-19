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

    public synchronized void unsubscribeAll(Client client) {
        Set<String> channels = clientChannels.get(client);
        if (channels != null) {
            Set<String> channelsCopy = Set.copyOf(channels);
            for (String channel : channelsCopy) {
                unsubscribe(client, channel);
            }
        }
    }

    public synchronized void unsubscribe(Client client, String channel) {
        // Remove channel from client subscriptions
        Set<String> clientSubs = clientChannels.get(client);
        if (clientSubs == null || !clientSubs.remove(channel)) {
            return;
        }

        if (clientSubs.isEmpty()) {
            clientChannels.remove(client);
        }

        // Remove client from channel subscriptions
        Set<Client> subscribers = channelSubscriptions.get(channel);
        if (subscribers != null) {
            subscribers.remove(client);
            if (subscribers.isEmpty()) {
                channelSubscriptions.remove(channel);
            }
        }

        client.decrementSubscriptionCount();
    }

    public Set<String> getClientChannels(Client client) {
        Set<String> channels = clientChannels.get(client);
        return channels != null ? Set.copyOf(channels) : Set.of();
    }
}

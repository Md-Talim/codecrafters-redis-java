package redis.configuration;

import java.util.List;
import java.util.function.Function;

public class RemoteOption extends Option {
    public RemoteOption(String name) {
        super(name, List.of(
                new Argument<String>("host", Function.identity()),
                new PortArgument()
        ));
    }

    public Argument<String> hostArgument() {
        return getArgumentAt(0, String.class);
    }

    public Argument<Integer> portArgument() {
        return getArgumentAt(1, Integer.class);
    }
}

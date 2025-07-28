package redis.stream.identifier;

import java.util.regex.Pattern;

public sealed interface Identifier permits MillisecondsIdentifier, UniqueIdentifier, WildcardIdentifier {
    public static final Pattern PATTERN = Pattern.compile("^(\\d+)(?:-(\\d+|\\*))?$");

    public static Identifier parse(String input) {
        if (WildcardIdentifier.INSTANCE.toString().equals(input)) {
            return WildcardIdentifier.INSTANCE;
        }

        if ("-".equals(input)) {
            return UniqueIdentifier.MIN;
        }
        if ("+".equals(input)) {
            return UniqueIdentifier.MAX;
        }

        var matcher = PATTERN.matcher(input);
        if (!matcher.find()) {
            throw new IllegalArgumentException("ERR Invalid stream ID specified as stream command argument");
        }

        var milliseconds = Long.parseLong(matcher.group(1));
        var sequenceNumber = matcher.group(2);
        if (sequenceNumber == null) {
            return new UniqueIdentifier(milliseconds, 0l);
        }
        if ("*".equals(sequenceNumber)) {
            return new MillisecondsIdentifier(milliseconds);
        }

        return new UniqueIdentifier(milliseconds, Long.parseLong(sequenceNumber));
    }
}

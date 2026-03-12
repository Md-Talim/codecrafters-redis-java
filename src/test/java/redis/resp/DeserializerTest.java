package redis.resp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.SimpleString;

class DeserializerTest {

    private Deserializer deserializer(String input) {
        var stream = new ByteArrayInputStream(
            input.getBytes(StandardCharsets.UTF_8)
        );
        return new Deserializer(stream);
    }

    @Test
    void parseSimpleString() throws Exception {
        var result = deserializer("+OK\r\n").read();

        assertThat(result).isInstanceOf(SimpleString.class);
        assertThat(((SimpleString) result).getValue()).isEqualTo("OK");
    }

    @Test
    void parseBulkString() throws Exception {
        var result = deserializer("$5\r\nhello\r\n").read();

        assertThat(result).isInstanceOf(BulkString.class);
        assertThat(((BulkString) result).getValue()).isEqualTo("hello");
    }

    @Test
    void parseEmptyBulkString() throws Exception {
        var result = deserializer("$0\r\n\r\n").read();

        assertThat(result).isInstanceOf(BulkString.class);
        assertThat(((BulkString) result).getValue()).isEqualTo("");
    }

    @Test
    void parseNullBulkString() throws Exception {
        var result = deserializer("$-1\r\n").read();

        assertThat(result).isNull();
    }

    @Test
    void parseArray() throws Exception {
        var result = deserializer("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n").read();

        assertThat(result).isInstanceOf(RArray.class);
        var array = (RArray) result;
        assertThat(array.size()).isEqualTo(2);
    }

    @Test
    void parseInlineCommand() throws Exception {
        var result = deserializer("PING\r\n").read();

        assertThat(result).isInstanceOf(RArray.class);
        var array = (RArray) result;
        assertThat(array.getCommandName()).isEqualToIgnoringCase("PING");
    }

    @Test
    void returnsNullOnEmptyStream() throws Exception {
        assertThat(deserializer("").read()).isNull();
    }

    @Test
    void parseBulkStringMissingCRLF() {
        assertThatThrownBy(() -> deserializer("$5\r\nhello").read())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Expected \\r");
    }

    @Test
    void parseBulkStringMissingLF() {
        assertThatThrownBy(() -> deserializer("$5\r\nhello\r").read())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Expected \\n after \\r");
    }
}

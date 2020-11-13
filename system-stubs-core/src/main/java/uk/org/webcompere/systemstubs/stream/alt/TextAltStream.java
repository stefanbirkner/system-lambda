package uk.org.webcompere.systemstubs.stream.alt;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * A string stream uses a ByteArray to provide a single string as an input
 */
public class TextAltStream extends DecoratingAltStream {
    /**
     * Construct with the string to use as an input
     * @param string the input string
     */
    public TextAltStream(String string) {
        super(new ByteArrayInputStream(string.getBytes(Charset.defaultCharset())));
    }
}

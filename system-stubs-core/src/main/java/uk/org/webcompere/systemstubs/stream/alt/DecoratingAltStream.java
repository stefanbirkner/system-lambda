package uk.org.webcompere.systemstubs.stream.alt;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decorates one stream by delegating to another
 */
public class DecoratingAltStream extends AltInputStream {
    private InputStream decoratee;

    public DecoratingAltStream(InputStream decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public int read() throws IOException {
        return decoratee.read();
    }

    @Override
    public boolean contains(Class<? extends InputStream> stream) {
        return super.contains(stream) || stream.isAssignableFrom(decoratee.getClass())
            || (decoratee instanceof AltInputStream && ((AltInputStream)decoratee).contains(stream));
    }
}

package uk.org.webcompere.systemstubs.stream.alt;

import java.io.InputStream;

/**
 * Base class for alternative input streams
 */
public abstract class AltInputStream extends InputStream {
    /**
     * Is this input stream of the given type, or a decorator of a stream of the
     * given type?
     * @param stream type of stream
     * @return true if there is already one of these in the chain
     */
    public boolean contains(Class<? extends InputStream> stream) {
        return stream.isAssignableFrom(this.getClass());
    }
}

package uk.org.webcompere.systemstubs.stream.alt;

import java.io.IOException;
import java.util.Objects;

public class ThrowAtEndStream extends DecoratingAltStream {
    private IOException ioException;
    private RuntimeException runtimeException;

    public ThrowAtEndStream(AltInputStream decoratee, IOException ioException) {
        super(decoratee);
        this.ioException = Objects.requireNonNull(ioException);
    }

    public ThrowAtEndStream(AltInputStream decoratee, RuntimeException runtimeException) {
        super(decoratee);
        this.runtimeException = Objects.requireNonNull(runtimeException);
    }

    @Override
    public int read() throws IOException {
        int next = super.read();
        if (next == -1) {
            throwException();
        }
        return next;
    }

    private void throwException() throws IOException {
        if (ioException != null) {
            throw ioException;
        }
        throw runtimeException;
    }
}

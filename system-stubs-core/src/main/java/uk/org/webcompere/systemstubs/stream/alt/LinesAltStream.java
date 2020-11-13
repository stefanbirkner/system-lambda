package uk.org.webcompere.systemstubs.stream.alt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;

/**
 * Provides lines of text from a source stream as an input stream
 */
public class LinesAltStream extends AltInputStream {
    private Iterator<Byte> byteIterator;

    /**
     * Given a stream of lines, supply them as an input stream
     * @param lines lines as a stream
     */
    public LinesAltStream(String ... lines) {
        this(Arrays.stream(lines), true);
    }

    /**
     * Given a stream of lines, supply them as an input stream
     * @param lines lines as a stream
     */
    public LinesAltStream(Stream<String> lines) {
        this(lines, true);
    }

    /**
     * Given a stream of lines, supply them as an input stream with optional line breaks added
     * @param lines the lines of the stream
     * @param addLineBreak whether to add a line break
     */
    public LinesAltStream(Stream<String> lines, boolean addLineBreak) {
        Stream<String> source = addLineBreak ? lines.flatMap(line -> Stream.of(line, lineSeparator())) : lines;

        byteIterator = source.flatMap(LinesAltStream::toByteArrayStream)
            .iterator();
    }

    @Override
    public int read() throws IOException {
        return byteIterator.hasNext() ? byteIterator.next() : -1;
    }

    private static Stream<Byte> toByteArrayStream(String s) {
        byte[] bytes = s.getBytes(Charset.defaultCharset());
        return IntStream.range(0, bytes.length)
            .mapToObj(i -> bytes[i]);
    }
}

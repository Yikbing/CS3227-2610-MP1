package studytracker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import studytracker.exception.StorageException;
import studytracker.model.StudySession;

class SessionFileCodecTest {
    private static final String VALID_LINE =
            "1\tQ1MzMjI3\tUGFyc2luZw==\t90\t2026-08-17\tTm90ZXM=";
    private final SessionFileCodec codec = new SessionFileCodec();

    // Verifies decoding against fixed format data independently of the encoder.
    @Test
    void decode_knownVersionedRecord_returnsExpectedSession() throws StorageException {
        StudySession result = codec.decode(VALID_LINE, 1);

        assertEquals(new StudySession("CS3227", "Parsing", 90,
                LocalDate.of(2026, 8, 17), "Notes"), result);
    }

    // Verifies encoding against a fixed versioned representation independently of the decoder.
    @Test
    void encode_knownSession_returnsExpectedVersionedRecord() {
        StudySession session = new StudySession("CS3227", "Parsing", 90,
                LocalDate.of(2026, 8, 17), "Notes");

        assertEquals(VALID_LINE, codec.encode(session));
    }

    // Verifies unsupported versions and incorrect field counts are rejected.
    @ParameterizedTest
    @ValueSource(strings = {
        "2\tQ1MzMjI3\tUGFyc2luZw==\t90\t2026-08-17\tTm90ZXM=",
        "1\tQ1MzMjI3\tUGFyc2luZw=="
    })
    void decode_unsupportedVersionOrFieldCount_throwsStorageException(String line) {
        assertThrows(StorageException.class, () -> codec.decode(line, 4));
    }

    // Verifies invalid Base64 text is translated into a storage-level error.
    @Test
    void decode_invalidEncodedField_throwsStorageException() {
        String line = "1\t***\tUGFyc2luZw==\t90\t2026-08-17\tTm90ZXM=";

        assertThrows(StorageException.class, () -> codec.decode(line, 2));
    }

    // Verifies malformed numeric and date fields are translated into storage errors.
    @ParameterizedTest
    @ValueSource(strings = {
        "1\tQ1MzMjI3\tUGFyc2luZw==\tminutes\t2026-08-17\tTm90ZXM=",
        "1\tQ1MzMjI3\tUGFyc2luZw==\t90\t2026-02-30\tTm90ZXM="
    })
    void decode_invalidDurationOrDate_throwsStorageException(String line) {
        assertThrows(StorageException.class, () -> codec.decode(line, 3));
    }

    // Verifies decoded records must still satisfy StudySession domain constraints.
    @ParameterizedTest
    @ValueSource(strings = {
        "1\t\tUGFyc2luZw==\t90\t2026-08-17\tTm90ZXM=",
        "1\tQ1MzMjI3\tUGFyc2luZw==\t0\t2026-08-17\tTm90ZXM="
    })
    void decode_invalidSessionValues_throwsStorageException(String line) {
        assertThrows(StorageException.class, () -> codec.decode(line, 5));
    }

    // Verifies a decode failure identifies the caller-supplied source line.
    @Test
    void decode_invalidRecord_reportsSuppliedLineNumber() {
        StorageException exception = assertThrows(
                StorageException.class, () -> codec.decode("invalid", 7));

        assertTrue(exception.getMessage().contains("line 7"));
    }
}

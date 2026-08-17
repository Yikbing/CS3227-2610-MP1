package studytracker.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import studytracker.exception.StorageException;
import studytracker.model.StudySession;

/** Loads and atomically saves study sessions in a local text file. */
public class Storage {
    private final Path dataPath;
    private final SessionFileCodec codec = new SessionFileCodec();

    public Storage(Path dataPath) {
        this.dataPath = dataPath;
    }

    public List<StudySession> load() throws StorageException {
        if (!Files.exists(dataPath)) {
            return List.of();
        }
        try {
            List<String> lines = Files.readAllLines(dataPath, StandardCharsets.UTF_8);
            List<StudySession> sessions = new ArrayList<>();
            for (int index = 0; index < lines.size(); index++) {
                if (!lines.get(index).isBlank()) {
                    sessions.add(codec.decode(lines.get(index), index + 1));
                }
            }
            return sessions;
        } catch (IOException exception) {
            throw new StorageException("Could not read saved sessions from " + dataPath + ".", exception);
        }
    }

    /**
     * Writes to a temporary sibling first so a failed save is less likely to damage existing data.
     */
    public void save(List<StudySession> sessions) throws StorageException {
        Path absolutePath = dataPath.toAbsolutePath();
        Path parent = absolutePath.getParent();
        Path temporaryPath = absolutePath.resolveSibling(absolutePath.getFileName() + ".tmp");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<String> lines = sessions.stream().map(codec::encode).toList();
            Files.write(temporaryPath, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryPath, absolutePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporaryPath, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new StorageException("Could not save study sessions to " + dataPath + ".", exception);
        }
    }
}

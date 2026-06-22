package org.nbu.citb408.olympics.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.nbu.citb408.olympics.competition.Biathlon;

/** Serializes and deserializes {@link Biathlon} objects via Java serialization. */
public final class BiathlonSerializer {

    /** Serializes {@code biathlon} to {@code target}. */
    public void save(Biathlon biathlon, Path target) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (ObjectOutputStream out =
                         new ObjectOutputStream(Files.newOutputStream(target))) {
                out.writeObject(biathlon);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to serialize Biathlon to " + target, e);
        }
    }

    /** Deserializes a {@link Biathlon} from {@code source}. */
    public Biathlon load(Path source) {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(source))) {
            return (Biathlon) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new UncheckedIOException(
                    new IOException("Failed to deserialize Biathlon from " + source, e));
        }
    }
}

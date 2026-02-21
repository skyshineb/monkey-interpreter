package com.coolstuff.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScriptSourceLoader {

    private ScriptSourceLoader() {
    }

    public static String load(Path path) throws ScriptLoadException {
        if (!Files.exists(path)) {
            throw new ScriptLoadException(path, "File not found");
        }

        if (!Files.isRegularFile(path)) {
            throw new ScriptLoadException(path, "Not a file");
        }

        if (!Files.isReadable(path)) {
            throw new ScriptLoadException(path, "File is not readable");
        }

        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exc) {
            throw new ScriptLoadException(path, "Failed to read file");
        }
    }

    public static final class ScriptLoadException extends Exception {
        private final Path path;

        private ScriptLoadException(Path path, String reason) {
            super(reason);
            this.path = path;
        }

        public String toCliMessage() {
            return "%s: %s".formatted(getMessage(), path);
        }
    }
}

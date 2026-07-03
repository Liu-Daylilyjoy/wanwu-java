package com.unicomai.wanwu.service.bff.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class UploadedFileStore {

    private static final UploadedFileStore DEFAULT = new UploadedFileStore(
            Paths.get(System.getProperty("java.io.tmpdir"), "wanwu-java-uploads"));
    private final Path root;

    private UploadedFileStore(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    static UploadedFileStore defaultStore() {
        return DEFAULT;
    }

    String readText(String fileId) {
        if (isBlank(fileId)) {
            return "";
        }
        Path path = filePath(fileId);
        if (!Files.isRegularFile(path)) {
            return "";
        }
        try {
            long size = Files.size(path);
            if (size > 5 * 1024 * 1024) {
                return "";
            }
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    void writeText(String fileId, String content) throws IOException {
        Path path = filePath(fileId);
        Files.createDirectories(path.getParent());
        Files.write(path, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
    }

    private Path filePath(String fileId) {
        return root.resolve("files").resolve(safeFileName(extractFileId(fileId))).normalize();
    }

    private String extractFileId(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return safeFileName(index >= 0 ? normalized.substring(index + 1) : normalized);
    }

    private String safeFileName(String value) {
        String name = isBlank(value) ? "upload.bin" : value.replace('\\', '_').replace('/', '_');
        name = name.replace("..", "_").trim();
        return name.isEmpty() ? "upload.bin" : name;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

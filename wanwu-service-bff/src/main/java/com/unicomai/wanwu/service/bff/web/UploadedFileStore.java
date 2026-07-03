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
        byte[] bytes = readBytes(fileId);
        return bytes.length == 0 ? "" : new String(bytes, StandardCharsets.UTF_8);
    }

    byte[] readBytes(String fileId) {
        if (isBlank(fileId)) {
            return new byte[0];
        }
        Path path = filePath(fileId);
        if (!Files.isRegularFile(path)) {
            return new byte[0];
        }
        try {
            long size = Files.size(path);
            if (size > 5 * 1024 * 1024) {
                return new byte[0];
            }
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    void writeText(String fileId, String content) throws IOException {
        writeBytes(fileId, (content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
    }

    void writeBytes(String fileId, byte[] content) throws IOException {
        Path path = filePath(fileId);
        Files.createDirectories(path.getParent());
        Files.write(path, content == null ? new byte[0] : content);
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

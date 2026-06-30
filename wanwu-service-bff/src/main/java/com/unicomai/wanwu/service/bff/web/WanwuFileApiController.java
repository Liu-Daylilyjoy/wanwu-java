package com.unicomai.wanwu.service.bff.web;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
public class WanwuFileApiController {

    private static final String SERVICE_PREFIX = "/service/api/v1";
    private static final String USER_PREFIX = "/user/api/v1";
    private static final String OPENURL_PREFIX = "/service/url/openurl/v1";
    private static final String LEGACY_OPENURL_PREFIX = "/openurl/v1";
    private static final int STATUS_FAILED = 0;
    private static final int STATUS_SUCCESS = 1;

    private final Path root;

    public WanwuFileApiController() {
        this(Paths.get(System.getProperty("java.io.tmpdir"), "wanwu-java-uploads"));
    }

    public WanwuFileApiController(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @GetMapping({SERVICE_PREFIX + "/file/check", USER_PREFIX + "/file/check"})
    public FrontendResponse<Map<String, Object>> checkFile(
            @RequestParam("fileName") String fileName,
            @RequestParam("sequence") int sequence,
            @RequestParam("chunkName") String chunkName) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", Files.exists(chunkPath(fileName, sequence, chunkName)) ? STATUS_SUCCESS : STATUS_FAILED);
        return FrontendResponse.ok(body);
    }

    @GetMapping({SERVICE_PREFIX + "/file/check/list",
            SERVICE_PREFIX + "/file/check/chunk/list",
            USER_PREFIX + "/file/check/list",
            USER_PREFIX + "/file/check/chunk/list"})
    public FrontendResponse<Map<String, Object>> checkFileList(@RequestParam("chunkName") String chunkName) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("uploadedFileSequences", uploadedSequences(chunkName));
        return FrontendResponse.ok(body);
    }

    @PostMapping({SERVICE_PREFIX + "/file/upload",
            USER_PREFIX + "/file/upload",
            OPENURL_PREFIX + "/file/upload",
            LEGACY_OPENURL_PREFIX + "/file/upload"})
    public FrontendResponse<Map<String, Object>> uploadChunk(
            @RequestParam("fileName") String fileName,
            @RequestParam("sequence") int sequence,
            @RequestParam("chunkName") String chunkName,
            @RequestParam(value = "files", required = false) MultipartFile file,
            @RequestParam(value = "file", required = false) MultipartFile fallbackFile) {
        try {
            MultipartFile actualFile = actualFile(file, fallbackFile);
            if (actualFile == null || actualFile.isEmpty()) {
                throw new IllegalArgumentException("files is required");
            }
            Path path = chunkPath(fileName, sequence, chunkName);
            Files.createDirectories(path.getParent());
            actualFile.transferTo(path.toFile());
            return FrontendResponse.ok(statusBody());
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "file upload failed: " + ex.getMessage());
        }
    }

    @PostMapping({SERVICE_PREFIX + "/file/merge",
            USER_PREFIX + "/file/merge",
            OPENURL_PREFIX + "/file/merge",
            LEGACY_OPENURL_PREFIX + "/file/merge"})
    public FrontendResponse<Map<String, Object>> mergeFile(@RequestBody(required = false) Map<String, Object> request) {
        try {
            Map<String, Object> body = request == null ? Collections.<String, Object>emptyMap() : request;
            String fileName = text(body, "fileName");
            String chunkName = text(body, "chunkName");
            int chunkTotal = intValue(body.get("chunkTotal"), 0);
            long fileSize = longValue(body.get("fileSize"), 0L);
            if (isBlank(fileName) || isBlank(chunkName) || chunkTotal <= 0) {
                throw new IllegalArgumentException("fileName, chunkName and chunkTotal are required");
            }
            List<Path> chunks = chunkFiles(chunkName);
            if (chunks.size() != chunkTotal) {
                throw new IllegalArgumentException("file upload not completed");
            }
            String fileId = newFileId(fileName);
            Path target = filePath(fileId);
            Files.createDirectories(target.getParent());
            long bytes = copyChunks(chunks, target);
            if (fileSize > 0 && bytes != fileSize) {
                Files.deleteIfExists(target);
                throw new IllegalArgumentException("merge file size mismatch");
            }
            deleteDirectory(chunkRoot(chunkName));
            return FrontendResponse.ok(mergeBody(fileName, fileId, bytes));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "file merge failed: " + ex.getMessage());
        }
    }

    @PostMapping({SERVICE_PREFIX + "/file/clean",
            USER_PREFIX + "/file/clean",
            OPENURL_PREFIX + "/file/clean",
            LEGACY_OPENURL_PREFIX + "/file/clean"})
    public FrontendResponse<Map<String, Object>> cleanFile(@RequestBody(required = false) Map<String, Object> request) {
        try {
            String chunkName = text(request, "chunkName");
            if (!isBlank(chunkName)) {
                deleteDirectory(chunkRoot(chunkName));
            }
            return FrontendResponse.ok(statusBody());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "file clean failed: " + ex.getMessage());
        }
    }

    @DeleteMapping({SERVICE_PREFIX + "/file/delete", USER_PREFIX + "/file/delete"})
    public FrontendResponse<Map<String, Object>> deleteFile(@RequestBody(required = false) Map<String, Object> request) {
        try {
            for (String fileId : fileList(request)) {
                Files.deleteIfExists(filePath(extractFileId(fileId)));
            }
            return FrontendResponse.ok(statusBody());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "file delete failed: " + ex.getMessage());
        }
    }

    @PostMapping({SERVICE_PREFIX + "/file/upload/direct",
            USER_PREFIX + "/file/upload/direct",
            "/service/api/openapi/v1/file/upload/direct"})
    public FrontendResponse<Map<String, Object>> directUpload(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            List<MultipartFile> uploadFiles = files == null ? new ArrayList<MultipartFile>() : new ArrayList<MultipartFile>(files);
            if (file != null) {
                uploadFiles.add(file);
            }
            if (uploadFiles.isEmpty()) {
                throw new IllegalArgumentException("files is required");
            }
            List<Map<String, Object>> saved = new ArrayList<Map<String, Object>>();
            for (MultipartFile uploadFile : uploadFiles) {
                if (uploadFile != null && !uploadFile.isEmpty()) {
                    saved.add(saveDirectFile(uploadFile));
                }
            }
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("files", saved);
            return FrontendResponse.ok(body);
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "direct file upload failed: " + ex.getMessage());
        }
    }

    @PostMapping({SERVICE_PREFIX + "/proxy/file/upload", SERVICE_PREFIX + "/inferpub/upload"})
    public FrontendResponse<Map<String, Object>> proxyUpload(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile files,
            @RequestParam(value = "file_name", required = false) String fileName) {
        try {
            MultipartFile actualFile = actualFile(file, files);
            Map<String, Object> saved;
            if (actualFile != null && !actualFile.isEmpty()) {
                saved = saveDirectFile(actualFile);
            } else {
                String fallbackName = defaultIfBlank(fileName, "proxy-upload.txt");
                String fileId = newFileId(fallbackName);
                Path target = filePath(fileId);
                Files.createDirectories(target.getParent());
                Files.write(target, new byte[0]);
                saved = fileBody(fallbackName, fileId, 0L);
            }
            Map<String, Object> body = new LinkedHashMap<String, Object>(saved);
            body.put("download_link", saved.get("filePath"));
            body.put("downloadLink", saved.get("filePath"));
            return FrontendResponse.ok(body);
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "proxy file upload failed: " + ex.getMessage());
        }
    }

    @GetMapping({SERVICE_PREFIX + "/file/download/{fileId:.+}", USER_PREFIX + "/file/download/{fileId:.+}"})
    public ResponseEntity<InputStreamResource> download(@PathVariable("fileId") String fileId) throws IOException {
        Path path = filePath(extractFileId(fileId));
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        InputStream input = Files.newInputStream(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + extractFileId(fileId) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(input));
    }

    private MultipartFile actualFile(MultipartFile primary, MultipartFile fallback) {
        return primary == null ? fallback : primary;
    }

    private Map<String, Object> statusBody() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("status", STATUS_SUCCESS);
        return body;
    }

    private Map<String, Object> mergeBody(String originalFileName, String fileId, long fileSize) {
        Map<String, Object> body = fileBody(originalFileName, fileId, fileSize);
        body.put("originalFileName", originalFileName);
        body.put("fileName", fileId);
        return body;
    }

    private Map<String, Object> saveDirectFile(MultipartFile uploadFile) throws IOException {
        String originalName = defaultIfBlank(uploadFile.getOriginalFilename(), "upload.bin");
        String fileId = newFileId(originalName);
        Path target = filePath(fileId);
        Files.createDirectories(target.getParent());
        uploadFile.transferTo(target.toFile());
        return fileBody(originalName, fileId, uploadFile.getSize());
    }

    private Map<String, Object> fileBody(String originalName, String fileId, long fileSize) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("fileName", originalName);
        body.put("fileId", fileId);
        body.put("filePath", SERVICE_PREFIX + "/file/download/" + fileId);
        body.put("fileSize", fileSize);
        return body;
    }

    private List<String> fileList(Map<String, Object> request) {
        if (request == null) {
            return Collections.emptyList();
        }
        Object value = request.get("fileList");
        if (value instanceof List) {
            List<String> result = new ArrayList<String>();
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        String text = value == null ? "" : String.valueOf(value);
        if (isBlank(text)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String item : text.split(",")) {
            if (!isBlank(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }

    private List<Integer> uploadedSequences(String chunkName) {
        try {
            List<Integer> sequences = new ArrayList<Integer>();
            Path dir = chunkDir(chunkName);
            if (!Files.isDirectory(dir)) {
                return sequences;
            }
            for (Path path : directoryFiles(dir)) {
                int sequence = sequence(path.getFileName().toString());
                if (sequence > 0) {
                    sequences.add(sequence);
                }
            }
            Collections.sort(sequences);
            return sequences;
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    private List<Path> chunkFiles(String chunkName) throws IOException {
        List<Path> files = directoryFiles(chunkDir(chunkName));
        Collections.sort(files, new Comparator<Path>() {
            @Override
            public int compare(Path left, Path right) {
                return Integer.valueOf(sequence(left.getFileName().toString()))
                        .compareTo(sequence(right.getFileName().toString()));
            }
        });
        return files;
    }

    private List<Path> directoryFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return Collections.emptyList();
        }
        List<Path> files = new ArrayList<Path>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        try {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    files.add(path);
                }
            }
        } finally {
            stream.close();
        }
        return files;
    }

    private long copyChunks(List<Path> chunks, Path target) throws IOException {
        long total = 0L;
        OutputStream output = Files.newOutputStream(target);
        try {
            byte[] buffer = new byte[8192];
            for (Path chunk : chunks) {
                InputStream input = Files.newInputStream(chunk);
                try {
                    int read;
                    while ((read = input.read(buffer)) >= 0) {
                        output.write(buffer, 0, read);
                        total += read;
                    }
                } finally {
                    input.close();
                }
            }
        } finally {
            output.close();
        }
        return total;
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> paths = new ArrayList<Path>();
        Files.walk(dir).forEach(paths::add);
        Collections.sort(paths, new Comparator<Path>() {
            @Override
            public int compare(Path left, Path right) {
                return right.compareTo(left);
            }
        });
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

    private Path chunkPath(String fileName, int sequence, String chunkName) {
        return chunkDir(chunkName).resolve(String.format(Locale.ROOT, "%010d_%s", sequence, safeFileName(fileName)))
                .normalize();
    }

    private Path chunkDir(String chunkName) {
        return chunkRoot(chunkName).resolve("upload").normalize();
    }

    private Path chunkRoot(String chunkName) {
        return root.resolve("chunks").resolve(md5(defaultIfBlank(chunkName, "default"))).normalize();
    }

    private Path filePath(String fileId) {
        return root.resolve("files").resolve(safeFileName(fileId)).normalize();
    }

    private String newFileId(String fileName) {
        String ext = "";
        String safeName = safeFileName(fileName);
        int dot = safeName.lastIndexOf('.');
        if (dot >= 0) {
            ext = safeName.substring(dot);
        }
        return UUID.randomUUID().toString().replace("-", "") + ext;
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
        String name = defaultIfBlank(value, "upload.bin").replace('\\', '_').replace('/', '_');
        name = name.replace("..", "_").trim();
        return name.isEmpty() ? "upload.bin" : name;
    }

    private int sequence(String name) {
        int index = name.indexOf('_');
        if (index <= 0) {
            return -1;
        }
        try {
            return Integer.parseInt(name.substring(0, index));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private long longValue(Object value, long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? defaultValue : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}

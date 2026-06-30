package com.unicomai.wanwu.service.bff.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WanwuBotUploadController {

    @PostMapping(value = "/bot/upload_file", consumes = MediaType.APPLICATION_JSON_VALUE)
    public FrontendResponse<Map<String, Object>> uploadBase64(
            @RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = request == null ? Collections.<String, Object>emptyMap() : request;
        String fileType = fileType(body.get("file_head"));
        String data = body.get("data") == null ? "" : String.valueOf(body.get("data"));
        return FrontendResponse.ok(uploadResult(fileType, data));
    }

    @PostMapping(value = "/bot/upload_file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FrontendResponse<Map<String, Object>> uploadMultipart(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile files) {
        try {
            MultipartFile actual = file == null ? files : file;
            if (actual == null || actual.isEmpty()) {
                return FrontendResponse.failure(1001, "file is empty");
            }
            String fileType = extension(actual.getOriginalFilename());
            String data = Base64.getEncoder().encodeToString(actual.getBytes());
            return FrontendResponse.ok(uploadResult(fileType, data));
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private Map<String, Object> uploadResult(String fileType, String data) {
        String safeType = isBlank(fileType) ? "png" : fileType;
        String uploadUri = "memory://bot/" + UUID.randomUUID().toString() + "." + safeType;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("upload_uri", uploadUri);
        result.put("upload_url", dataUrl(safeType, data));
        return result;
    }

    @SuppressWarnings("unchecked")
    private String fileType(Object fileHead) {
        if (fileHead instanceof Map) {
            Object value = ((Map<String, Object>) fileHead).get("file_type");
            if (value == null) {
                value = ((Map<String, Object>) fileHead).get("fileType");
            }
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase();
    }

    private String dataUrl(String fileType, String data) {
        String contentType = "image/" + ("jpg".equalsIgnoreCase(fileType) ? "jpeg" : fileType.toLowerCase());
        return "data:" + contentType + ";base64," + (data == null ? "" : data);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

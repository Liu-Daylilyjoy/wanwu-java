package com.unicomai.wanwu.service.bff.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WanwuServiceCompatApiController {

    @PostMapping("/service/api/v1/rag/upload")
    public FrontendResponse<Map<String, Object>> ragUpload(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "markdown", required = false, defaultValue = "false") boolean markdown) {
        try {
            if (files == null || files.isEmpty()) {
                return FrontendResponse.failure(1001, "file is empty");
            }
            List<Map<String, Object>> fileList = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String fileName = originalFileName(file);
                String fileUrl = dataUrl(file);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("fileIndex", i);
                item.put("fileUrl", markdown ? markdownImage(fileName, fileUrl) : fileUrl);
                fileList.add(item);
            }
            if (fileList.isEmpty()) {
                return FrontendResponse.failure(1001, "file is empty");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileList", fileList);
            return FrontendResponse.ok(result);
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        }
    }

    private String originalFileName(MultipartFile file) {
        String name = defaultIfBlank(file.getOriginalFilename(), "file");
        String normalized = name.replace("\\", "/");
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }

    private String dataUrl(MultipartFile file) throws IOException {
        String contentType = defaultIfBlank(file.getContentType(), "application/octet-stream");
        return "data:" + contentType + ";base64,"
                + Base64.getEncoder().encodeToString(file.getBytes());
    }

    private String markdownImage(String fileName, String fileUrl) {
        String alt = defaultIfBlank(fileName, "file").replace("[", "").replace("]", "");
        return "![" + alt + "](" + fileUrl + ")";
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

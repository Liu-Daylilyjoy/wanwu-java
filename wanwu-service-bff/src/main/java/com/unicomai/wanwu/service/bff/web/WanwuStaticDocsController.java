package com.unicomai.wanwu.service.bff.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WanwuStaticDocsController {

    private static final String DOCS_PREFIX = "/user/api/v1/static/docs";
    private static final String MANUAL_PREFIX = "/user/api/v1/static/manual";
    private static final String MANUAL_CLASSPATH_PREFIX = "static/manual/";
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final Map<String, List<String>> CSV_TEMPLATES = csvTemplates();
    private static final Map<String, List<String>> XLSX_TEMPLATES = xlsxTemplates();

    @GetMapping(DOCS_PREFIX + "/{fileName:.+}")
    public ResponseEntity<byte[]> downloadStaticDoc(@PathVariable("fileName") String fileName) {
        if (CSV_TEMPLATES.containsKey(fileName)) {
            return response(fileName, MediaType.parseMediaType("text/csv; charset=utf-8"),
                    csv(CSV_TEMPLATES.get(fileName)));
        }
        if (XLSX_TEMPLATES.containsKey(fileName)) {
            try {
                return response(fileName, MediaType.parseMediaType(XLSX_CONTENT_TYPE),
                        xlsx(XLSX_TEMPLATES.get(fileName)));
            } catch (IOException ex) {
                return ResponseEntity.status(500).body(new byte[0]);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(MANUAL_PREFIX + "/**")
    public ResponseEntity<byte[]> downloadManualResource(HttpServletRequest request) {
        String path = manualResourcePath(request);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new ClassPathResource(MANUAL_CLASSPATH_PREFIX + path);
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] body = readBytes(resource);
            return ResponseEntity.ok()
                    .contentType(mediaType(path))
                    .contentLength(body.length)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .body(body);
        } catch (IOException ex) {
            return ResponseEntity.status(500).body(new byte[0]);
        }
    }

    private static ResponseEntity<byte[]> response(String fileName, MediaType mediaType, byte[] body) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(body.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(body);
    }

    private static String manualResourcePath(HttpServletRequest request) {
        String prefix = MANUAL_PREFIX + "/";
        String requestUri = request.getRequestURI();
        int index = requestUri.indexOf(prefix);
        if (index < 0) {
            return null;
        }
        String rawPath = requestUri.substring(index + prefix.length());
        if (rawPath.length() == 0) {
            return null;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(rawPath, StandardCharsets.UTF_8.name());
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (java.io.UnsupportedEncodingException ex) {
            return null;
        }
        String path = decoded.replace('\\', '/');
        if (path.length() == 0 || path.startsWith("/") || path.indexOf('\0') >= 0 || path.indexOf(':') >= 0) {
            return null;
        }
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() == 0 || ".".equals(parts[i]) || "..".equals(parts[i])) {
                return null;
            }
        }
        return path;
    }

    private static byte[] readBytes(Resource resource) throws IOException {
        InputStream input = resource.getInputStream();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

    private static MediaType mediaType(String path) {
        String value = path.toLowerCase(Locale.ROOT);
        if (value.endsWith(".md")) {
            return MediaType.parseMediaType("text/markdown; charset=utf-8");
        }
        if (value.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (value.endsWith(".jpg") || value.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (value.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (value.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        }
        if (value.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private static byte[] csv(List<String> headers) {
        StringBuilder builder = new StringBuilder();
        builder.append(joinCsv(headers)).append("\r\n");
        builder.append(joinCsv(sampleRow(headers))).append("\r\n");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] xlsx(List<String> headers) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            zip(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                            + "</Types>");
            zip(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                            + "</Relationships>");
            zip(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                            + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                            + "<sheets><sheet name=\"template\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                            + "</workbook>");
            zip(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                            + "</Relationships>");
            zip(zip, "xl/styles.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                            + "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                            + "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>"
                            + "<borders count=\"1\"><border/></borders>"
                            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                            + "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
                            + "</styleSheet>");
            zip(zip, "xl/worksheets/sheet1.xml", worksheet(headers));
        } finally {
            zip.close();
        }
        return out.toByteArray();
    }

    private static String worksheet(List<String> headers) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        builder.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        row(builder, 1, headers);
        row(builder, 2, sampleRow(headers));
        builder.append("</sheetData></worksheet>");
        return builder.toString();
    }

    private static void row(StringBuilder builder, int rowNumber, List<String> cells) {
        builder.append("<row r=\"").append(rowNumber).append("\">");
        for (int index = 0; index < cells.size(); index++) {
            builder.append("<c r=\"").append(column(index)).append(rowNumber).append("\" t=\"inlineStr\"><is><t>");
            builder.append(xml(cells.get(index)));
            builder.append("</t></is></c>");
        }
        builder.append("</row>");
    }

    private static String column(int index) {
        StringBuilder builder = new StringBuilder();
        int value = index;
        do {
            builder.insert(0, (char) ('A' + value % 26));
            value = value / 26 - 1;
        } while (value >= 0);
        return builder.toString();
    }

    private static List<String> sampleRow(List<String> headers) {
        String[] samples = new String[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            samples[i] = "example_" + headers.get(i).replaceAll("[^A-Za-z0-9]+", "_").toLowerCase();
        }
        return Arrays.asList(samples);
    }

    private static String joinCsv(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(csvCell(values.get(i)));
        }
        return builder.toString();
    }

    private static String csvCell(String value) {
        String text = value == null ? "" : value;
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static String xml(String value) {
        String text = value == null ? "" : value;
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static void zip(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static Map<String, List<String>> csvTemplates() {
        Map<String, List<String>> templates = new LinkedHashMap<String, List<String>>();
        templates.put("qa_pair_template.csv", Arrays.asList("question", "answer", "similar_questions"));
        templates.put("report.csv", Arrays.asList("title", "content", "source"));
        templates.put("segment.csv", Arrays.asList("content", "metadata"));
        return Collections.unmodifiableMap(templates);
    }

    private static Map<String, List<String>> xlsxTemplates() {
        Map<String, List<String>> templates = new LinkedHashMap<String, List<String>>();
        templates.put("users.xlsx", Arrays.asList("username", "nickname", "password", "email", "phone", "role"));
        templates.put("sensitive.xlsx", Arrays.asList("Political", "Revile", "Pornography", "ViolentTerror",
                "Illegal", "InformationSecurity", "Other"));
        templates.put("graph_schema.xlsx", Arrays.asList("node", "relation", "target", "description"));
        templates.put("url_import_template.xlsx", Arrays.asList("name", "url"));
        templates.put("qa_import_template.xlsx", Arrays.asList("question", "answer", "similar_questions"));
        return Collections.unmodifiableMap(templates);
    }
}

package com.unicomai.wanwu.service.bff.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class SimpleXlsxWriter {

    private SimpleXlsxWriter() {
    }

    static byte[] write(String sheetName, List<List<Object>> rows) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8);
            add(zip, "[Content_Types].xml", contentTypes());
            add(zip, "_rels/.rels", rootRelationships());
            add(zip, "xl/workbook.xml", workbook(sheetName));
            add(zip, "xl/_rels/workbook.xml.rels", workbookRelationships());
            add(zip, "xl/styles.xml", styles());
            add(zip, "xl/worksheets/sheet1.xml", sheet(rows));
            zip.close();
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("create xlsx failed", ex);
        }
    }

    private static void add(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    private static String rootRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(String sheetName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + xml(cleanSheetName(sheetName)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private static String workbookRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
                + "</styleSheet>";
    }

    private static String sheet(List<List<Object>> rows) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        if (rows != null) {
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                xml.append("<row r=\"").append(rowIndex + 1).append("\">");
                List<Object> row = rows.get(rowIndex);
                if (row != null) {
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        cell(xml, rowIndex + 1, colIndex + 1, row.get(colIndex));
                    }
                }
                xml.append("</row>");
            }
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private static void cell(StringBuilder xml, int rowIndex, int colIndex, Object value) {
        String ref = column(colIndex) + rowIndex;
        if (value instanceof Number) {
            xml.append("<c r=\"").append(ref).append("\"><v>")
                    .append(value)
                    .append("</v></c>");
            return;
        }
        xml.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                .append(xml(value == null ? "" : String.valueOf(value)))
                .append("</t></is></c>");
    }

    private static String column(int index) {
        StringBuilder result = new StringBuilder();
        int current = index;
        while (current > 0) {
            current--;
            result.insert(0, (char) ('A' + (current % 26)));
            current = current / 26;
        }
        return result.toString();
    }

    private static String cleanSheetName(String value) {
        String name = value == null || value.trim().isEmpty() ? "Sheet1" : value.trim();
        name = name.replaceAll("[\\\\/?*\\[\\]:]", " ");
        return name.length() > 31 ? name.substring(0, 31) : name;
    }

    private static String xml(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&apos;");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }
}

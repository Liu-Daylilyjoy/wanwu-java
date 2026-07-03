package com.unicomai.wanwu.service.bff.web;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class SimpleXlsxReader {

    private SimpleXlsxReader() {
    }

    static String toDelimitedText(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        try {
            WorkbookXml workbook = readWorkbook(bytes);
            if (workbook.sheetXml.isEmpty()) {
                return "";
            }
            List<String> sharedStrings = sharedStrings(workbook.sharedStringsXml);
            List<List<String>> rows = rows(workbook.sheetXml, sharedStrings);
            return delimited(rows);
        } catch (Exception ex) {
            return "";
        }
    }

    private static WorkbookXml readWorkbook(byte[] bytes) throws Exception {
        WorkbookXml workbook = new WorkbookXml();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if ("xl/sharedStrings.xml".equals(name)) {
                    workbook.sharedStringsXml = text(zip);
                } else if ("xl/worksheets/sheet1.xml".equals(name) && workbook.sheetXml.isEmpty()) {
                    workbook.sheetXml = text(zip);
                } else if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")
                        && workbook.sheetXml.isEmpty()) {
                    workbook.sheetXml = text(zip);
                }
            }
        } finally {
            zip.close();
        }
        return workbook;
    }

    private static String text(ZipInputStream zip) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = zip.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static List<String> sharedStrings(String xml) throws Exception {
        List<String> result = new ArrayList<>();
        if (xml == null || xml.trim().isEmpty()) {
            return result;
        }
        NodeList nodes = elements(parse(xml), "si");
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(joinText((Element) nodes.item(i), "t"));
        }
        return result;
    }

    private static List<List<String>> rows(String xml, List<String> sharedStrings) throws Exception {
        List<List<String>> result = new ArrayList<>();
        NodeList rows = elements(parse(xml), "row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);
            List<String> values = new ArrayList<>();
            NodeList cells = children(row, "c");
            for (int j = 0; j < cells.getLength(); j++) {
                Element cell = (Element) cells.item(j);
                int index = columnIndex(cell.getAttribute("r"), j);
                while (values.size() <= index) {
                    values.add("");
                }
                values.set(index, cellValue(cell, sharedStrings));
            }
            trim(values);
            if (!values.isEmpty()) {
                result.add(values);
            }
        }
        return result;
    }

    private static String cellValue(Element cell, List<String> sharedStrings) {
        String type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            return joinText(cell, "t").trim();
        }
        String value = firstText(cell, "v").trim();
        if ("s".equals(type)) {
            try {
                int index = Integer.parseInt(value);
                return index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index).trim() : "";
            } catch (NumberFormatException ex) {
                return "";
            }
        }
        return value;
    }

    private static String delimited(List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        for (List<String> row : rows) {
            if (result.length() > 0) {
                result.append('\n');
            }
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    result.append('\t');
                }
                result.append(row.get(i));
            }
        }
        return result.toString();
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception ignored) {
            // The JDK parser already keeps this local, and unsupported features should not break template parsing.
        }
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static NodeList elements(Document document, String localName) {
        NodeList nodes = document.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? document.getElementsByTagName(localName) : nodes;
    }

    private static NodeList children(Element parent, String localName) {
        List<Node> nodes = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String nodeName = node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
            if (node instanceof Element && localName.equals(nodeName)) {
                nodes.add(node);
            }
        }
        return new ListNodeList(nodes);
    }

    private static String firstText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(localName);
        }
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private static String joinText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(localName);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.append(nodes.item(i).getTextContent());
        }
        return result.toString();
    }

    private static int columnIndex(String reference, int fallback) {
        if (reference == null || reference.isEmpty()) {
            return fallback;
        }
        int result = 0;
        boolean found = false;
        for (int i = 0; i < reference.length(); i++) {
            char ch = Character.toUpperCase(reference.charAt(i));
            if (ch < 'A' || ch > 'Z') {
                break;
            }
            found = true;
            result = result * 26 + (ch - 'A' + 1);
        }
        return found ? result - 1 : fallback;
    }

    private static void trim(List<String> values) {
        for (int i = values.size() - 1; i >= 0; i--) {
            if (values.get(i) != null && !values.get(i).trim().isEmpty()) {
                return;
            }
            values.remove(i);
        }
    }

    private static final class WorkbookXml {
        private String sharedStringsXml = "";
        private String sheetXml = "";
    }

    private static final class ListNodeList implements NodeList {
        private final List<Node> nodes;

        private ListNodeList(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Node item(int index) {
            return nodes.get(index);
        }

        @Override
        public int getLength() {
            return nodes.size();
        }
    }
}

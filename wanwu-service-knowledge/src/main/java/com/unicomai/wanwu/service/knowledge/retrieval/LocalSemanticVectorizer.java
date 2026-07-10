package com.unicomai.wanwu.service.knowledge.retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class LocalSemanticVectorizer {

    public static final String MODEL_ID = "local-hash-v1";
    private static final int DIMENSIONS = 256;

    public List<Double> vectorize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        double[] values = new double[DIMENSIONS];
        String normalized = text.toLowerCase(Locale.ENGLISH).trim();
        for (String token : tokens(normalized)) {
            int hash = token.hashCode();
            int index = (hash & Integer.MAX_VALUE) % DIMENSIONS;
            values[index] += 1.0D;
        }
        double norm = 0D;
        for (double value : values) {
            norm += value * value;
        }
        if (norm <= 0D) {
            return Collections.emptyList();
        }
        norm = Math.sqrt(norm);
        List<Double> result = new ArrayList<Double>(DIMENSIONS);
        for (double value : values) {
            result.add(value / norm);
        }
        return result;
    }

    public double cosine(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || left.size() != right.size()) {
            return 0D;
        }
        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int i = 0; i < left.size(); i++) {
            double a = left.get(i) == null ? 0D : left.get(i);
            double b = right.get(i) == null ? 0D : right.get(i);
            dot += a * b;
            leftNorm += a * a;
            rightNorm += b * b;
        }
        if (leftNorm <= 0D || rightNorm <= 0D) {
            return 0D;
        }
        double cosine = dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
        return Math.max(0D, Math.min(1D, cosine));
    }

    private List<String> tokens(String text) {
        List<String> result = new ArrayList<String>();
        StringBuilder word = new StringBuilder();
        StringBuilder cjk = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch) && !Character.isIdeographic(ch)) {
                word.append(ch);
                flushCjk(result, cjk);
            } else if (Character.isIdeographic(ch)) {
                flushWord(result, word);
                cjk.append(ch);
            } else {
                flushWord(result, word);
                flushCjk(result, cjk);
            }
        }
        flushWord(result, word);
        flushCjk(result, cjk);
        return result;
    }

    private void flushWord(List<String> result, StringBuilder word) {
        if (word.length() == 0) {
            return;
        }
        String value = word.toString();
        result.add("w:" + value);
        for (int i = 0; i + 2 < value.length(); i++) {
            result.add("g:" + value.substring(i, i + 3));
        }
        word.setLength(0);
    }

    private void flushCjk(List<String> result, StringBuilder cjk) {
        if (cjk.length() == 0) {
            return;
        }
        for (int i = 0; i < cjk.length(); i++) {
            result.add("c:" + cjk.charAt(i));
            if (i + 1 < cjk.length()) {
                result.add("b:" + cjk.substring(i, i + 2));
            }
        }
        cjk.setLength(0);
    }
}

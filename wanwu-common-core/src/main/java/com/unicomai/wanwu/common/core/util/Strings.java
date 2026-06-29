package com.unicomai.wanwu.common.core.util;

public final class Strings {

    private Strings() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

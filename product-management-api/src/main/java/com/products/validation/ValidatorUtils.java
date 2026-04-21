package com.products.validation;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ValidatorUtils {

    private ValidatorUtils() {
    }

    private static final Pattern INPUT_PATTERN =
            Pattern.compile("^[.\\p{Alnum}\\p{Space}]{1,1024}$");

    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    public static boolean isAnyNull(Object... values) {
        if (values == null) {
            return true;
        }

        for (Object value : values) {
            if (Objects.isNull(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidInput(String value) {
        return value != null
                && !value.isBlank()
                && INPUT_PATTERN.matcher(value).matches();
    }
}
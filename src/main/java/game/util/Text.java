package game.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Text {
    public static String snakeCase(final String text) {
        final StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (final char c : text.toCharArray()) {
            if (c == ' ' || c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static String camelCase(final String text) {
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(String token : tokens(text)) {
            if (isFirst) {
                sb.append(token.toLowerCase());
                isFirst = false;
            } else {
                sb.append(capitalized(token));
            }
        }

        return sb.toString();
    }

    public static String allWordsCapitalized(final String text) {
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(String token : tokens(text)) {
            String cToken = capitalized(token);
            if (!isFirst) {
                sb.append(" ");
                isFirst = false;
            }
            sb.append(cToken);
        }

        return sb.toString();
    }

    public static String capitalized(final String text) {
        String trimmed = text.trim();
        if (text==null || text.isEmpty()) {
            return text;
        }
        return trimmed.substring(0, 0).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    public static List<String> tokens(final String text) {
        return Arrays.asList(text.split("\\s+"));
    }
}

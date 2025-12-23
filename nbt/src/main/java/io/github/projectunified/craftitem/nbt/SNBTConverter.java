package io.github.projectunified.craftitem.nbt;

import java.util.List;
import java.util.Map;

/**
 * Converts Java objects to SNBT (Minecraft's text NBT format).
 *
 * <p>Converts maps, lists, arrays, and primitives to SNBT strings with proper formatting. Supports
 * data component format with brackets and equals syntax.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * SNBTConverter.convert(Map.of("key", 42, "name", "test"));
 * // Returns: {key:42,name:"test"}
 *
 * SNBTConverter.convert(Map.of("key", 42), true);
 * // Returns: [key=42]
 * }</pre>
 */
public final class SNBTConverter {
    private SNBTConverter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Converts a value to SNBT string format
     *
     * @param value The value to convert
     * @return SNBT formatted string
     */
    public static String convert(Object value) {
        return convert(value, false);
    }

    /**
     * Converts a value to SNBT string format
     *
     * @param value                  The value to convert
     * @param useDataComponentFormat If true, use Minecraft data component format
     * @return SNBT formatted string
     */
    public static String convert(Object value, boolean useDataComponentFormat) {
        if (value == null) {
            return useDataComponentFormat ? "[]" : "{}";
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return convertToCompound(map, useDataComponentFormat);
        }
        // For primitives and arrays, use convertValue
        return convertValue(value);
    }

    /**
     * Converts a value to SNBT format based on its Java type
     */
    private static String convertValue(Object value) {
        if (value == null) {
            return "\"\"";
        }

        // Check for raw NBT data
        if (value instanceof NBTRaw) {
            return ((NBTRaw) value).value;
        }

        // Check for map
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return convertToCompound(map, false);
        }

        // Handle primitive types
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Byte) {
            return value + "b";
        }
        if (value instanceof Short) {
            return value + "s";
        }
        if (value instanceof Integer) {
            return value.toString();
        }
        if (value instanceof Long) {
            return value + "L";
        }
        if (value instanceof Float) {
            return value + "f";
        }
        if (value instanceof Double) {
            return value.toString();
        }

        // Handle strings
        if (value instanceof String) {
            return convertStringValue((String) value);
        }

        // Handle collections and arrays
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return convertToList(list);
        }
        if (value instanceof byte[]) {
            return convertToByteArray((byte[]) value);
        }
        if (value instanceof int[]) {
            return convertToIntArray((int[]) value);
        }
        if (value instanceof long[]) {
            return convertToLongArray((long[]) value);
        }

        // Fallback to string representation
        return escapeString(value.toString());
    }

    private static String convertToCompound(Map<String, Object> map, boolean useDataComponentFormat) {
        StringBuilder sb = new StringBuilder();
        sb.append(useDataComponentFormat ? "[" : "{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;

            String key = entry.getKey();
            sb.append(escapeKey(key)).append(useDataComponentFormat ? "=" : ":").append(convertValue(entry.getValue()));
        }

        sb.append(useDataComponentFormat ? "]" : "}");
        return sb.toString();
    }

    private static String convertToList(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(convertValue(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String convertToByteArray(byte[] arr) {
        StringBuilder sb = new StringBuilder("[B;");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]).append("b");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String convertToIntArray(int[] arr) {
        StringBuilder sb = new StringBuilder("[I;");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String convertToLongArray(long[] arr) {
        StringBuilder sb = new StringBuilder("[L;");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]).append("L");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Converts a string value, checking for numeric literals with suffixes
     */
    private static String convertStringValue(String str) {
        str = str.trim();

        // Try parsing as number with suffix (byte, short, long, float, double)
        String numberWithSuffix = tryParseNumberWithSuffix(str);
        if (numberWithSuffix != null) {
            return numberWithSuffix;
        }

        // Try parsing as plain number (int or double)
        String plainNumber = tryParseNumber(str);
        if (plainNumber != null) {
            return plainNumber;
        }

        // Not a number, treat as string
        return escapeString(str);
    }

    /**
     * Attempts to parse a string as a number with a type suffix
     */
    private static String tryParseNumberWithSuffix(String str) {
        if (str.length() < 2) {
            return null;
        }

        char lastChar = str.charAt(str.length() - 1);
        String numPart = str.substring(0, str.length() - 1);

        try {
            switch (lastChar) {
                case 'b':
                case 'B':
                    Byte.parseByte(numPart);
                    return str.toLowerCase().replace('B', 'b');
                case 's':
                case 'S':
                    Short.parseShort(numPart);
                    return str.toLowerCase().replace('S', 's');
                case 'l':
                case 'L':
                    Long.parseLong(numPart);
                    return str.toUpperCase().replace('l', 'L');
                case 'f':
                case 'F':
                    Float.parseFloat(numPart);
                    return str.toLowerCase().replace('F', 'f');
                case 'd':
                case 'D':
                    Double.parseDouble(numPart);
                    return numPart;
            }
        } catch (NumberFormatException e) {
            // Not a valid number with this suffix
        }

        return null;
    }

    /**
     * Attempts to parse a string as a plain number (int or double)
     */
    private static String tryParseNumber(String str) {
        try {
            if (str.contains(".")) {
                Double.parseDouble(str);
            } else {
                Integer.parseInt(str);
            }
            return str;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Escapes a string for SNBT format
     */
    private static String escapeString(String str) {
        if (needsQuotes(str)) {
            return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return str;
    }

    /**
     * Escapes a key for SNBT format
     */
    private static String escapeKey(String key) {
        if (needsQuotesForKey(key)) {
            return "\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return key;
    }

    /**
     * Checks if a string needs quotes
     */
    private static boolean needsQuotes(String str) {
        if (str.isEmpty()) {
            return true;
        }

        char first = str.charAt(0);
        if (Character.isDigit(first) || first == '-' || first == '.' || first == '+') {
            return true;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.' && c != '+') {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a key needs quotes
     */
    private static boolean needsQuotesForKey(String key) {
        if (key.isEmpty()) {
            return true;
        }

        for (char c : key.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.' && c != '+') {
                return true;
            }
        }
        return false;
    }
}

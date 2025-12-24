package io.github.projectunified.craftitem.nbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Normalizes maps and converts them to the right types for SNBT.
 *
 * <p>Recursively converts map and list values to properly typed values. Supports custom string
 * translators for dynamic substitution and forced-value maps using {@code $type} and {@code $value} keys.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * Map<String, Object> map = Map.of(
 *     "$type", "float",
 *     "$value", "123.45"
 * );
 * Object result = NBTMapNormalizer.normalize(map);  // Returns 123.45f
 * }</pre>
 */
public final class NBTMapNormalizer {
    private NBTMapNormalizer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Normalizes a value by resolving forced-value maps and applying translator
     *
     * @param value The value to normalize
     * @return Normalized value (can be a Map, primitive, or array)
     * @throws IllegalArgumentException if forced-value map is invalid
     */
    public static Object normalize(Object value) {
        return normalize(value, s -> s);
    }

    /**
     * Normalizes a value by resolving forced-value maps and applying translator
     *
     * @param value      The value to normalize
     * @param translator Custom string translator for values
     * @return Normalized value (can be a Map, primitive, or array)
     * @throws IllegalArgumentException if forced-value map is invalid
     */
    public static Object normalize(Object value, UnaryOperator<String> translator) {
        if (value instanceof List) {
            return normalizeList((List<?>) value, translator);
        }

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;

            // Check if this is a forced-value map
            if (map.containsKey("$type")) {
                if (!map.containsKey("$value")) {
                    throw new IllegalArgumentException("Map with '$type' entry must also have '$value' entry");
                }
                return normalizeForcedValue(map.get("$type"), map.get("$value"), translator);
            }

            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), normalize(entry.getValue(), translator));
            }
            return result;
        }

        if (value instanceof String) {
            Number number = tryParseNumberWithSuffix(((String) value).trim());
            if (number != null) {
                return number;
            }
        }

        return value;
    }

    /**
     * Attempts to parse a string as a number with a type suffix
     */
    private static Number tryParseNumberWithSuffix(String str) {
        if (str.length() < 2) {
            return null;
        }

        char lastChar = str.charAt(str.length() - 1);
        String numPart = str.substring(0, str.length() - 1);

        try {
            switch (lastChar) {
                case 'b':
                case 'B':
                    return Byte.parseByte(numPart);
                case 's':
                case 'S':
                    return Short.parseShort(numPart);
                case 'l':
                case 'L':
                    return Long.parseLong(numPart);
                case 'f':
                case 'F':
                    return Float.parseFloat(numPart);
                case 'd':
                case 'D':
                    return Double.parseDouble(numPart);
                case 'i':
                case 'I':
                    return Integer.parseInt(numPart);
            }
        } catch (NumberFormatException e) {
            // Not a valid number with this suffix
        }

        return null;
    }

    /**
     * Normalizes a forced-value based on specified $type
     */
    private static Object normalizeForcedValue(Object type, Object value, UnaryOperator<String> translator) {
        if (!(type instanceof String)) {
            throw new IllegalArgumentException("Type must be a string");
        }

        String typeStr = ((String) type).toLowerCase();

        switch (typeStr) {
            case "byte":
                return normalizeToByte(value, translator);
            case "boolean":
                return normalizeToBoolean(value, translator);
            case "short":
                return normalizeToShort(value, translator);
            case "int":
            case "integer":
                return normalizeToInt(value, translator);
            case "long":
                return normalizeToLong(value, translator);
            case "float":
                return normalizeToFloat(value, translator);
            case "double":
                return normalizeToDouble(value, translator);
            case "string":
                return normalizeToString(value, translator);
            case "raw":
                return normalizeToRaw(value, translator);
            case "list":
                return normalizeToList(value, translator);
            case "compound":
                return normalize(value, translator);
            case "byte_array":
            case "bytearray":
                return normalizeToByteArray(value, translator);
            case "int_array":
            case "intarray":
                return normalizeToIntArray(value, translator);
            case "long_array":
            case "longarray":
                return normalizeToLongArray(value, translator);
            default:
                throw new IllegalArgumentException("Unknown type: " + typeStr);
        }
    }

    /**
     * Normalizes a list by recursively normalizing its elements
     */
    private static List<?> normalizeList(List<?> list, UnaryOperator<String> translator) {
        List<Object> result = new ArrayList<>();
        for (Object value : list) {
            result.add(normalize(value, translator));
        }
        return result;
    }

    // Normalization methods for forced values
    private static Byte normalizeToByte(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Byte.parseByte(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to byte");
    }

    private static Boolean normalizeToBoolean(Object value, UnaryOperator<String> translator) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            str = translator.apply(str);
            str = str.toLowerCase();
            if (str.equals("true")) return true;
            if (str.equals("false")) return false;
            try {
                int num = Integer.parseInt(str);
                return num != 0;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert " + value + " to boolean");
            }
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to boolean");
    }

    private static Short normalizeToShort(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Short.parseShort(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to short");
    }

    private static Integer normalizeToInt(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Integer.parseInt(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to int");
    }

    private static Long normalizeToLong(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Long.parseLong(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to long");
    }

    private static Float normalizeToFloat(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Float.parseFloat(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to float");
    }

    private static Double normalizeToDouble(Object value, UnaryOperator<String> translator) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            str = str.trim();
            str = translator.apply(str);
            return Double.parseDouble(str);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to double");
    }

    private static String normalizeToString(Object value, UnaryOperator<String> translator) {
        return translator.apply(value.toString());
    }

    private static Object normalizeToRaw(Object value, UnaryOperator<String> translator) {
        String rawValue = translator.apply(value.toString());
        return new NBTRaw(rawValue);
    }

    private static List<?> normalizeToList(Object value, UnaryOperator<String> translator) {
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Value must be a List");
        }
        return normalizeList((List<?>) value, translator);
    }

    private static byte[] normalizeToByteArray(Object value, UnaryOperator<String> translator) {
        byte[] arr;
        if (value instanceof byte[]) {
            arr = (byte[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToByte(item, translator);
                } else {
                    arr[i] = ((Number) item).byteValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be byte[] or List");
        }
        return arr;
    }

    private static int[] normalizeToIntArray(Object value, UnaryOperator<String> translator) {
        int[] arr;
        if (value instanceof int[]) {
            arr = (int[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToInt(item, translator);
                } else {
                    arr[i] = ((Number) item).intValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be int[] or List");
        }
        return arr;
    }

    private static long[] normalizeToLongArray(Object value, UnaryOperator<String> translator) {
        long[] arr;
        if (value instanceof long[]) {
            arr = (long[]) value;
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            arr = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof String) {
                    arr[i] = normalizeToLong(item, translator);
                } else {
                    arr[i] = ((Number) item).longValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Value must be long[] or List");
        }
        return arr;
    }
}
